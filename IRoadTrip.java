/* ------------------------------------------------------------------------ */
/* IROADTRIP.JAVA: analyzes borders.txt, state_num.tsv, and capdist.csv     */
/* to get border information on 253 countries. This program uses Dijkstra's */
/* shortest path algorithm to find the shortest path when embarking on an   */
/* "international road trip".                                               */
/* README.md is available on github.                                        */
/* ------------------------------------------------------------------------ */

import java.util.*;
import java.io.*;

public class IRoadTrip {
    /*-------------------------------------------------------------------*/
    /* global variables
    /*-------------------------------------------------------------------*/
    static final int NumCountries = 253;    // num of countries in borders.txt
    static final int NumStates = 216;       // num of countries in state_name.tsv
    static final int NumDistances = 41006;  // num of recorded distances in capdist.csv
    static final int block = 202 ;          // size of blocks in capdist.csv
    public static String [] countries = new String [NumCountries];	// country names
    final private static String [][] stateNums = new String [NumStates][2];	// state name, state number
    public static String [] line0 = new String [NumCountries];	// a line from file0, borders.txt
    public static AdjList [] adjListA  ;    // adjacency list array
    public static int [][] distances = new int [NumDistances] [3]; //capdist.csv: country1, country2, distance
    public static int [] numA_Arr = new int [block]; // arr of "numA" in capdist

    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);
        a3.acceptUserInput();
    }

    public IRoadTrip (String [] args) {
        /*-------------------------------------------------------------------*/
        /* constructor ; processing three files ; execute()
        /*-------------------------------------------------------------------*/
        if ( args.length != 3 ) {
            System.out.println( "** please attach: borders.txt, state_name.tsv, capdist.csv **" );
            System.exit( 0 );
        }
        try {
            // processing file0: borders.txt
            File borders = new File(args[0]);
            Scanner sc0 = new Scanner( borders );
            doFile0( sc0,line0,countries );

            // processing file1: state_name.tsv
            File state_name = new File(args[1]);
            Scanner sc1 = new Scanner( state_name );
            doFile1( sc1);

            // processing file2: capdist.csv
            File capdist = new File(args[2]);
            Scanner sc2 = new Scanner( capdist );
            doFile2( sc2,numA_Arr, distances);

        } catch(Exception e ) {
            System.out.println( "** I/O exception **" );
            System.exit( 0 );
        }
        execute() ;
    }

    /* EXECUTE(): method called by IRoadTrip constructor. The array of adjacency lists is constructed here. */
    public void execute(){

        sort0();    // stateNums[][] is sorted
        sort1();	// distance[][] is sorted

        adjListA = new AdjList [NumCountries];
        for ( int i = 0;  i < NumCountries;  i++ ) {
            int [] nabor = nextTo( line0[i] ); // indexes of neighboring countries in borders.txt
            adjListA[i] = new AdjList();

            if ( nabor[0] != -2 ) { // if there is a neighbor

                // for every neighboring country in borders.txt, ensure the country exists in state_name.
                // if so, add the new gNode (graph node) to the adjacency list
                for (int x : nabor) {
                    int y = findDistance(countries[i], countries[x], numA_Arr, distances);
                    if (y != -1) { // distance exists
                        adjListA[i].add(new gNode(x, y));
                    }
                }
            }
        }
    }

    /* GETDISTANCE(): returns the distance between two countries with land borders, if exists. */
    public static int getDistance (String country1, String country2) {
        Trip t = new Trip( countries, distances, adjListA, numA_Arr) ;
       int num1 = index(country1) ;
       int num2 = index(country2) ;
       if (num1 < 0 || num2 < 0){
           return (-1) ;
       }
       gNode p = adjListA[num1].head ;
       while(p != null){
           if(p.vrtx == num2){
               return(t.Distance(country1,country2)) ;
           }
           p = p.link ;
       }
       return(-1) ;
    }

    /* FINDPATH(): finds the shortest path between two countries and returns in List form */
    public static List<String> findPath (String country1, String country2) {
        Trip t = new Trip( countries, distances, adjListA, numA_Arr) ;

        List<String> roadPath = t.path( country1,country2 );
        return (roadPath) ;
    }


    /* ACCEPTUSERINPUT(): allows user to find the shortest path between countries */
    public void acceptUserInput() {
        Trip trip = new Trip( countries,distances,adjListA,numA_Arr);
        Scanner kbd = new Scanner( System.in );

        while ( true ) {
            System.out.print( "Enter the name of the  first "+
                    "country (type EXIT to quit): " );
            String x = kbd.nextLine(); // first country
            if ( x.equals( "EXIT" ) ) {
                break;
            }
            System.out.print( "Enter the name of the second "+
                    "country (type EXIT to quit): " );
            String y = kbd.nextLine(); // country we're going to
            if ( y.equals( "EXIT" ) ) {
                break;
            }
            int ix = index( x );
            int iy = index( y );
            if ( (ix < 0) || (iy < 0) || (ix == iy) ) { // country was the same or does not exist
                System.out.println( "Invalid country name. "+
                        "Please enter a valid country name." );
                continue;
            }
            List<String> roadPath = trip.path( x,y );
            System.out.println( "Route from "+x+" to "+y+":" );
            for (String s : roadPath) {
                if (!roadPath.contains("")){
                    System.out.print(" * ") ;
                }
                System.out.println(s);
            }
        }
        kbd.close();
    }

    /* FINDDISTANCE(): find the distance between two countries in the distance array using binary search. */
    private static int findDistance(String country1, String country2,int [] numaA,int [][] dist ) {
        int b,m,t;

        // get state number
        int num1 = num( country1 );
        int num2 = num( country2 );
        if ( num1 == num2 ) { // same country
            return( 0 );
        }

        // first: find the block where country1 is
        b = 0;
        t = 202;
        while ( b < t ) {
            m = ((b+t)/2);
            if ( num1 <= numaA[m] ) {
                t = m;
            } else {
                b = m+1;
            }
        }

        // determines when blocks start and end. block 64 is flawed, it is double the size of others
        b = (t <= 64 ? 202*t : 202*t+202);
        t = (t != 64 ? b+201 : b+403);

        // second: find the line in the block where country2 is
        while ( b < t ) {
            m = ((b+t)/2);
            if ( num2 <= dist[m][1] ) {
                t = m;
            } else {
                b = m+1;
            }
        }
        return( (dist[t][1] == num2 ? dist[t][2] : -1) );
    }

    /* GETNAME1(): finding the country name in file1 (state_name.tsv). */
    private static String getname1( String s ) {

        s = s.substring( s.indexOf( '\t' )+1 ); // delete first tab
        s = s.substring( s.indexOf( '\t' )+1 ); // delete second tab
        s = s.substring( 0,s.indexOf( '\t' ) ); // 0 is now moved over, 3rd field of the line
        // so s becomes actual name of country
        return( s );
    }

    /* INDEX(): finds index of a country in borders.txt. */
    private static int index( String name ) {
        int b,m,t;

        b = 0;
        t = countries.length-1;
        while ( b  < t ) {
            m = ((b+t) >> 1 );
            if ( name.compareTo( countries[m] ) <= 0 ) {
                t = m;
            } else {
                b = m+1;
            }
        }
        return( (name.equals( countries[t] ) ? t : -1) );
    }

    /* NEXTTO(): returns array of countries adjacent to s, using borders.txt. */
    private static int [] nextTo( String s ) {

        // replacing all '=' with ';'
        s = s.replace( '=',';' );
        if ( s.length() == s.indexOf( ';' ) + 2 ) {
            int [] y = new int [1];
            y[0] = -2;			// no neighbor
            return( y );
        }
        int [] x = new int [countries.length];	// very temporary
        int n = 0;
        while ( s.indexOf( ';' ) != -1 ) {
            s = s.substring( s.indexOf( ';' )+2 );
            int i = -1;
            do {
                i++; // where ; is
            } while ( !Character.isDigit( s.charAt( i ) ) );
            String t = s.substring( 0,i-1 );
            x[n++] = index( t ); // n = number of neighbors
            s = s.substring( i );
        }
        int [] y = new int [n];
        System.arraycopy(x, 0, y, 0, n);
        return( y );
    }

    /* NUM(): finds country number in stateNums using binary search */
    private static int num( String s ) {
        int b,m,t;

        b = 0;
        t = stateNums.length-1;
        while ( b < t ) {
            m = ((b+t) >> 1);
            if ( s.compareTo( stateNums[m][0] ) <= 0 ) {
                t = m;
            } else {
                b = m+1;
            }
        }
        // returns state number as an int
        return( Integer.parseInt( stateNums[t][1] ) );
    }

    /* SORT0(): uses bubble sort to sort the state names in stateNums[][] in alphabetical order. */
    private static void sort0() {
        int n = IRoadTrip.stateNums.length;

        for ( int i = 1;  i < n;  i++ ) {
            for ( int j = 0;  j < n-i;  j++ ) {
                // if s1.compareTo(s2) is negative, s1 should proceed s2 alphabetically
                if ( IRoadTrip.stateNums[j+1][0].compareTo( IRoadTrip.stateNums[j][0] ) < 0  ) {

                    // first: swap strings in column 0, which is the state name
                    String s = IRoadTrip.stateNums[j][0];
                    IRoadTrip.stateNums[j][0] = IRoadTrip.stateNums[j+1][0];
                    IRoadTrip.stateNums[j+1][0]= s;

                    // second: swap strings in column 1, which is the state number
                    s = IRoadTrip.stateNums[j][1];
                    IRoadTrip.stateNums[j][1] = IRoadTrip.stateNums[j+1][1];
                    IRoadTrip.stateNums[j+1][1] = s;
                }
            }
        }
    }

    /* SORT1(): uses bubble sort to sort each block in the distances[][], keying on numB. */
    private static void sort1() {
        int beginBlock,endBlock,i,j,k,x;

        for ( i = 0;  i < block;  i++ ) {
            // determines when blocks start and end. block 64 is flawed, it is double the size of others
            int a = (i <= 64 ? 202*i : 202*i+202); // begin block
            int b = (i != 64 ? a+201 : a+403); // end block

            for ( j = 1;  j < b-a+1;  j++ ) {
                for ( k = 0;  k < b-a+1-j;  k++ ) {
                    // reminder: we are in a block, so column 0 (numa) will be the same
                    if ( distances[a+k+1][1] < distances[a+k][1] ) {

                        // first: swap numb (country 2)
                        x = distances[a+k][1];
                        distances[a+k][1] = distances[a+k+1][1];
                        distances[a+k+1][1] = x;

                        // second: swap distance
                        x = distances[a+k][2];
                        distances[a+k][2] = distances[a+k+1][2];
                        distances[a+k+1][2] = x;
                    }
                }
            }
        }
    }

    private static class gNode { // a single node, graph node

        /*-----------------------------------------------------------*/
        /* data members: gNode
        /*-----------------------------------------------------------*/

        private int vrtx; // index in borders.txt
        private int wght; // distance in cap dist
        private gNode link; // a pointer to the next node

        /*-----------------------------------------------------------*/
        /* constructor: gNode
        /*-----------------------------------------------------------*/

        gNode( int v,int d ) {
            vrtx = v;
            wght = d;
            link = null;
        }
    }

    public static class AdjList { // a linked list of gNodes

        /*-----------------------------------------------------------*/
        /* data members: AdjList
        /*-----------------------------------------------------------*/

        private gNode head, tail;

        /*-----------------------------------------------------------*/
        /* constructor: AdjList
        /*-----------------------------------------------------------*/

        AdjList() {
            head = tail = null; // a linked list
        }

        /*-----------------------------------------------------------*/
		/* methods: AdjList
        /*-----------------------------------------------------------*/

        /* ADD(): adds gnodes to an adjacency list increasing order by vertex */
        private void add( gNode q ) {

            if ( head == null ) {		// empty list?
                head = tail = q;
                return;
            }
            if ( q.vrtx < head.vrtx ) {	// at front?
                q.link = head;
                head = q;
                return;
            }
            if ( tail.vrtx < q.vrtx ) {	// at back?
                tail.link = q;
                tail = q;
                return;
            }
            gNode p = head;			// or in the middle?
            gNode r = p.link;
            while ( r.vrtx < q.vrtx ) {
                p = r;
                r = r.link;
            }
            p.link = q;
            q.link = r;
        }

        private gNode Head() {
            return( head );
        }
    }

    private static class qNode {			// queue node, for Dijkstra's

        /*-----------------------------------------------------------*/
        /* data members - qNode
        /*-----------------------------------------------------------*/

        private qNode blink; // backwards link
        private int vrtx;
        private int dist;
        private qNode flink; // forwards link

        /*-----------------------------------------------------------*/
        /* constructor - qNode
        /*-----------------------------------------------------------*/

        qNode( int v,int d ) {

            blink = flink = null;
            vrtx = v;
            dist = d;
        }
    }

    private static class Queue { // will be a doubly linked list

        /*-----------------------------------------------------------*/
        /* data members: Queue
        /*-----------------------------------------------------------*/

        private qNode head;
        private qNode tail;

        /*-----------------------------------------------------------*/
        /* constructor: Queue
        /*-----------------------------------------------------------*/

        Queue() {
            head = tail = null;
        }

        /*-----------------------------------------------------------*/
        /* methods: Queue
        /*-----------------------------------------------------------*/

        /* ADD(): adds qNodes to a doubly linked list, which will be the queue.
        The qnode with the minimum distance will be in the front */
        private void add( qNode q ) {

            if ( tail == null ) {		// empty list?
                head = tail = q;
                return;
            }
            if ( q.dist < head.dist ) {	// at front?
                head.blink = q;
                q.flink = head;
                head = q;
                return;
            }
            if ( tail.dist <= q.dist ) {	// at rear?
                tail.flink = q;
                q.blink = tail;
                tail = q;
                return;
            }
            qNode p = head;			// in the middle?
            qNode r = p.flink;
            while ( r.dist < q.dist ) {
                p = r;
                r = r.flink;
            }
            p.flink = r.blink = q;
            q.blink = p;
            q.flink = r;
        }

        /* DECREASE(): update distance to every vertex adjacent to u, or country1 */
        private void decrease( int v,int newDist ) {

            qNode p = head;
            while ( p.vrtx != v ) {		// find qNode
                p = p.flink;
            }
            qNode q = new qNode( v,newDist );
            // replace
            remove( p );
            add( q );
        }

        /* DELETEMIN(): removes the first node from the queue */
        private int deleteMin() {

            int u = head.vrtx; // storing vertex of head
            head = head.flink;
            if ( head == null ) { // removing node
                tail = null;
            } else {
                head.blink = null;
            }
            return( u );
        }

        /* ISNOTDONE(): ensures the queue still holds neighbors  */
        private Boolean isNotDone() {
            return( (head != null ) &&
                    (head.dist != Integer.MAX_VALUE) );
        }

        /* REMOVE(): removes a node from queue */
        private void remove( qNode p ) {

            if ( p == head ) {
                head = p.flink;
            } else {
                p.blink.flink = p.flink;
            }
            if ( p == tail ) {
                tail = p.blink;
            } else {
                p.flink.blink = p.blink;
            }
        }
    }

    public static class Trip {

        /*-----------------------------------------------------------*/
        /* data members: Trip
        /*-----------------------------------------------------------*/

        public String [] country;
        public int [][] dist;
        public AdjList [] adjListA;
        public int [] numaA_Arr;

        /*-----------------------------------------------------------*/
        /* constructor: Trip
        /*-----------------------------------------------------------*/

        Trip( String [] a,int [][] b,AdjList [] c,
              int [] e) {

            country = a;
            dist = b;
            adjListA = c;
            numaA_Arr = e;
        }


        /*-----------------------------------------------------------*/
        /* methods - Trip
        /*-----------------------------------------------------------*/

        /* EXPLORE(): determine if a vertex is reachable from v */
        public void explore( int v,
                              Boolean [] visited ) {

            visited[v] = true;
            gNode p = adjListA[v].head;
            while ( p != null ) {
                if ( !visited[p.vrtx] ) {
                    explore( p.vrtx,visited );
                }
                p = p.link;
            }
        }

        /* DISTANCE(): searches block in capdist to find distance between 2 countries */
        public int Distance(String country1, String country2 ) {

            int numa = num( country1 );
            int numb = num( country2 );
            if ( numa == numb ) {
                return( 0 );
            }

            int b,m,t;

            // first: find the block where country1 is
            b = 0;
            t = 202;
            while ( b < t ) {
                m = ((b+t) >> 1);
                if ( numa <= numaA_Arr[m] ) {
                    t = m;
                } else {
                    b = m+1;
                }
            }

            // determines when blocks start and end. block 64 is flawed, it is double the size of others
            b = (t <= 64 ? 202*t : 202*t+202);
            t = (t != 64 ? b+201 : b+403);

            // second: find the line in the block where country2 is
            while ( b < t ) {
                m = ((b+t) >> 1);
                if ( numb <= dist[m][1] ) {
                    t = m;
                } else {
                    b = m+1;
                }
            }
            return( (dist[t][1] == numb ? dist[t][2] : -1) );
        }

        /* FINDMINPATHS(): implements Dijkstra's algorithm to find the shortest path between two vertices */
        public int [][] findMinPaths( int s ) {

            // dist[v] is the distance from s, starting vertex, to a vertex v
            Integer [] dist = new Integer [NumCountries];
            Integer [] prev = new Integer [NumCountries];
            Queue queue = new Queue();
            for ( int v = 0;  v < NumCountries;  v++ ) {
                // initializing
                dist[v] = Integer.MAX_VALUE;	// infinity
                prev[v] = -1;
            }
            dist[s] = 0; // every vertex is 0 km from itself, front of the queue
            for ( int v = 0;  v < NumCountries;  v++ ) {
                queue.add( new qNode( v,dist[v] ) );
            }
            while ( queue.isNotDone() ) {
                int u = queue.deleteMin();
                // search through every vertex v which is adjacent to u
                for ( gNode p = adjListA[u].Head();  p != null;  p = p.link ) {
                    int v = p.vrtx;
                    int newDist = dist[u]+p.wght;
                    if ( newDist < dist[v] ) {
                        dist[v] = newDist;
                        prev[v] = u;
                        queue.decrease( v,newDist );
                    }
                }
            }
            int [][] minPath = new int [NumCountries][NumCountries];
            for ( int v = 0;  v < NumCountries;  v++ ) {
                minPath[v][0] = dist[v];
                minPath[v][1] = prev[v];
            }
            // array with minimum path is returned
            return( minPath );
        }

        /* PATH(): calls findMinPaths to find the minimum path between two countries */
        public List<String> path( String country1,String country2 ) {

            List<String> y = new ArrayList<>();
            int v0 = index( country1 );
            int v1 = index( country2 );
            if (v0 < 0 || v1 < 0 ){
                y.add("") ;
                return(y) ;
            }
            if ( noPath( v0,v1,adjListA ) ) {
                y.add( "" );
                return( y );
            }
            int [][] minPath = findMinPaths( v0 );
            int l = 0;
            int [] path = new int [country.length];
            path[0] = v1;
            int i = v1;
            do { // unwinding the previous array from findMinPaths to retrieve actual path
                i = minPath[i][1];
                path[++l] = i;
            } while ( i != v0 );
            for ( int j = l;  1 <= j;  --j ) {
                String c0 = country[path[j]];
                String c1 = country[path[j-1]];
                int d = Distance( c0,c1 );
                // creating array of output
                String t = c0 + " --> "+c1+" ("+d+" km.)";
                y.add( t );
            }
            return( y );
        }

        /* NOPATH(): checks to see if there is a path between two vertices */
        private Boolean noPath( int v0,int v1,AdjList [] adjListA ) {

            int n = adjListA.length;
            Boolean [] visited = new Boolean [n];
            Arrays.fill(visited, false);
            explore( v0,visited );
            return( (!visited[v1]) );
        }

    }

    /* DOFILE0: processes borders.txt file and fills in country[] */
    private static void doFile0( Scanner sc,String [] line,String [] countries ) {

        // edited border.txt
        String [] newFile = {
                "Afghanistan = China 91 km; Iran 921 km; Pakistan 2,670 km; Tajikistan 1,357 km; Turkmenistan 804 km; Uzbekistan 144 km",
                "Akrotiri = Cyprus 48 km",
                "Albania = Greece 212 km; Kosovo 112 km; North Macedonia 181 km; Montenegro 186 km",
                "Algeria = Libya 989 km; Mali 1,359 km; Mauritania 460 km; Morocco 1,941 km; Niger 951 km; Tunisia 1,034 km",
                "American Samoa = ",
                "Andorra = France 55 km; Spain 63 km",
                "Angola = Congo, Democratic Republic of the 2,646 km; Congo, Republic of the 231 km; Namibia 1,427 km; Zambia 1,065 km",
                "Anguilla = ",
                "Antarctica = ",
                "Antigua and Barbuda = ",
                "Argentina = Bolivia 942 km; Brazil 1,263 km; Chile 6,691 km; Paraguay 2,531 km; Uruguay 541 km",
                "Armenia = Azerbaijan 996 km; Georgia 219 km; Iran 44 km; Turkey (Turkiye) 311 km",
                "Aruba = ",
                "Ashmore and Cartier Islands = ",
                "Australia = ",
                "Austria = Czechia 402 km; Germany 801 km; Hungary 321 km; Italy 404 km; Liechtenstein 34 km; Slovakia 105 km; Slovenia 299 km; Switzerland 158 km",
                "Azerbaijan = Armenia 996 km; Georgia 428 km; Iran 689 km; Russia 338 km; Turkey (Turkiye) 17 km",
                "Bahamas, The = ",
                "Bahrain = ",
                "Bangladesh = Burma 271 km; India 4,142 km",
                "Barbados = ",
                "Belarus = Latvia 161 km; Lithuania 640 km; Poland 375 km; Russia 1,312 km; Ukraine 1,111 km",
                "Belgium = France 556 km; Germany 133 km; Luxembourg 130 km; Netherlands 478 km",
                "Belize = Guatemala 266 km; Mexico 276 km",
                "Benin = Burkina Faso 386 km; Niger 277 km; Nigeria 809 km; Togo 651 km",
                "Bermuda = ",
                "Bhutan = China 477 km; India 659 km",
                "Bolivia = Argentina 942 km; Brazil 3,403 km; Chile 942 km; Paraguay 753 km; Peru 1,212 km",
                "Bosnia and Herzegovina = Croatia 956 km; Montenegro 242 km; Serbia 345 km",
                "Botswana = Namibia 1,544 km; South Africa 1,969 km; Zambia 0.15 km; Zimbabwe 834 km",
                "Bouvet Island = ",
                "Brazil = Argentina 1,263 km; Bolivia 3,403 km; Colombia 1,790 km; Guyana 1,308 km; Paraguay 1,371 km; Peru 2,659 km; Suriname 515 km; Uruguay 1,050 km; Venezuela 2,137 km",
                "British Indian Ocean Territory = ",
                "British Virgin Islands = ",
                "Brunei = Malaysia 266 km",
                "Bulgaria = Greece 472 km; North Macedonia 162 km; Romania 605 km; Serbia 344 km; Turkey (Turkiye) 223 km",
                "Burkina Faso = Benin 386 km; Cote d'Ivoire 545 km; Ghana 602 km; Mali 1325 km; Niger 622 km; Togo 131 km",
                "Burma = Bangladesh 271 km; China 2,129 km; India 1,468 km; Laos 238 km; Thailand 2,416 km",
                "Burundi = Congo, Democratic Republic of the 236 km; Rwanda 315 km; Tanzania 589 km",
                "Cabo Verde = ",
                "Cambodia = Laos 555 km; Thailand 817 km; Vietnam 1158 km",
                "Cameroon = Central African Republic 901 km; Chad 1,116 km; Congo, Republic of the 494 km; Equatorial Guinea 183 km; Gabon 349 km; Nigeria 1975 km",
                "Canada = United States 8,891 km; Denmark 1.3 km",
                "Cayman Islands = ",
                "Central African Republic = Cameroon 901 km; Chad 1556 km; Congo, Democratic Republic of the 1,747 km; Congo, Republic of the 487 km; South Sudan 1055 km; Sudan 174 km",
                "Chad = Cameroon 1,116 km; Central African Republic 1,556 km; Libya 1,050 km; Niger 1,196 km; Nigeria 85 km; Sudan 1,403 km",
                "Chile = Argentina 6,691 km; Bolivia 942 km; Peru 168 km",
                "China = Afghanistan 91 km; Bhutan 477 km; Burma 2,129 km; India 2,659 km; Kazakhstan 1,765 km; Korea, North 1,352 km; Kyrgyzstan 1,063 km; Laos 475 km; Mongolia 4,630 km; Nepal 1,389 km; Pakistan 438 km; Russia 4,133 km; Tajikistan 477 km; Vietnam 1,297 km",
                "Christmas Island = ",
                "Clipperton Island = ",
                "Cocos (Keeling) Islands = ",
                "Colombia = Brazil 1,790 km; Ecuador 708 km; Panama 339 km; Peru 1,494 km; Venezuela 2,341 km",
                "Comoros = ",
                "Congo, Democratic Republic of the = Angola 2,646 km; Burundi 236 km; Central African Republic 1,747 km; Congo, Republic of the 1,775 km; Rwanda 221 km; South Sudan 714 km; Tanzania 479 km; Uganda 877 km; Zambia 2,332 km",
                "Congo, Republic of the = Angola 231 km; Cameroon 494 km; Central African Republic 487 km; Congo, Democratic Republic of the 1,775 km; Gabon 2,567 km",
                "Cook Islands = ",
                "Coral Sea Islands = ",
                "Costa Rica = Nicaragua 313 km; Panama 348 km",
                "Cote d'Ivoire = Burkina Faso 545 km; Ghana 720 km; Guinea 816 km; Liberia 778 km; Mali 599 km",
                "Croatia = Bosnia and Herzegovina 956 km; Hungary 348 km; Montenegro 19 km; Serbia 314 km; Slovenia 600 km",
                "Cuba = ",
                "Curacao = ",
                "Cyprus = ",
                "Czechia = Austria 402 km; Germany 704 km; Poland 699 km; Slovakia 241 km",
                "Denmark = Germany 140 km; Canada 1.3 km",
                "Dhekelia = Cyprus 108 km",
                "Djibouti = Eritrea 125 km; Ethiopia 342 km; Somalia 61 km",
                "Dominica = ",
                "Dominican Republic = Haiti 376 km",
                "Ecuador = Colombia 708 km; Peru 1529 km",
                "Egypt = Gaza Strip 13 km; Israel 208 km; Libya 1,115 km; Sudan 1,276 km",
                "El Salvador = Guatemala 199 km; Honduras 391 km",
                "Equatorial Guinea = Cameroon 183 km; Gabon 345 km",
                "Eritrea = Djibouti 125 km; Ethiopia 1,033 km; Sudan 682 km",
                "Estonia = Latvia 333 km; Russia 324 km",
                "Eswatini = Mozambique 108 km; South Africa 438 km",
                "Ethiopia = Djibouti 342 km; Eritrea 1,033 km; Kenya 867 km; Somalia 1,640 km; South Sudan 1,299 km; Sudan 744 km",
                "Falkland Islands (Islas Malvinas) = ",
                "Faroe Islands = ",
                "Fiji = ",
                "Finland = Norway 709 km; Sweden 545 km; Russia 1,309 km",
                "France = Andorra 55 km; Belgium 556 km; Germany 418 km; Italy 476 km; Luxembourg 69 km; Monaco 6 km; Spain 646 km; Switzerland 525 km",
                "French Polynesia = ",
                "French Southern and Antarctic Lands = ",
                "Gabon = Cameroon 349 km; Congo, Republic of the 2,567 km; Equatorial Guinea 345 km",
                "Gambia, The = Senegal 749 km",
                "Gaza Strip = Egypt 13 km; Israel 59 km",
                "Georgia = Armenia 219 km; Azerbaijan 428 km; Russia 894 km; Turkey (Turkiye) 273 km",
                "Germany = Austria 801 km; Belgium 133 km; Czechia 704 km; Denmark 140 km; France 418 km; Luxembourg 128 km; Netherlands 575 km; Poland 447 km; Switzerland 348 km",
                "Ghana = Burkina Faso 602 km; Cote d'Ivoire 720 km; Togo 1098 km",
                "Gibraltar = Spain 1.2 km",
                "Greece = Albania 212 km; Bulgaria 472 km; North Macedonia 234 km; Turkey (Turkiye) 192 km",
                "Greenland = ",
                "Grenada = ",
                "Guam = ",
                "Guatemala = Belize 266 km; El Salvador 199 km; Honduras 244 km; Mexico 958 km",
                "Guernsey = ",
                "Guinea = Cote d'Ivoire 816 km; Guinea-Bissau 421 km; Liberia 590 km; Mali 1062 km; Senegal 363 km; Sierra Leone 794 km",
                "Guinea-Bissau = Guinea 421 km; Senegal 341 km",
                "Guyana = Brazil 1,308 km; Suriname 836 km; Venezuela 789 km",
                "Haiti = Dominican Republic 376 km",
                "Heard Island and McDonald Islands = ",
                "Holy See (Vatican City) = Italy 3.4 km",
                "Honduras = Guatemala 244 km; El Salvador 391 km; Nicaragua 940 km",
                "Hong Kong = ",
                "Hungary = Austria 321 km; Croatia 348 km; Romania 424 km; Serbia 164 km; Slovakia 627 km; Slovenia 94 km; Ukraine 128 km",
                "Iceland = ",
                "India = Bangladesh 4,142 km; Bhutan 659 km; Burma 1,468 km; China 2,659 km; Nepal 1,770 km; Pakistan 3,190 km",
                "Indonesia = Malaysia 1,881 km; Papua New Guinea 824 km; Timor-Leste 253 km",
                "Iran = Afghanistan 921 km; Armenia 44 km; Azerbaijan 689 km; Iraq 1,599 km; Pakistan 959 km; Turkey (Turkiye) 534 km; Turkmenistan 1,148 km",
                "Iraq = Iran 1,599 km; Jordan 179 km; Kuwait 254 km; Saudi Arabia 811 km; Syria 599 km; Turkey (Turkiye) 367 km",
                "Ireland = United Kingdom 490 km",
                "Isle of Man = ",
                "Israel = Egypt 208 km; Gaza Strip 59 km; Jordan 327 km; Lebanon 81 km; Syria 83 km; West Bank 330 km",
                "Italy = Austria 404 km; France 476 km; Holy See (Vatican City) 3.4 km; San Marino 37 km; Slovenia 218 km; Switzerland 698 km",
                "Jamaica = ",
                "Jan Mayen = ",
                "Japan = ",
                "Jersey = ",
                "Jordan = Iraq 179 km; Israel 307 km; Saudi Arabia 731 km; Syria 379 km; West Bank 148 km",
                "Kazakhstan = China 1,765 km; Kyrgyzstan 1,212 km; Russia 7,644 km; Turkmenistan 413 km; Uzbekistan 2,330 km",
                "Kenya = Ethiopia 867 km; Somalia 684 km; South Sudan 317 km; Tanzania 775 km; Uganda 814 km",
                "Kiribati = ",
                "Korea, North = China 1,352 km; Korea, South 237 km; Russia 18 km",
                "Korea, South = Korea, North 237 km",
                "Kosovo = Albania 112 km; North Macedonia 160 km; Montenegro 76 km; Serbia 366 km",
                "Kuwait = Iraq 254 km; Saudi Arabia 221 km",
                "Kyrgyzstan = China 1,063 km; Kazakhstan 1,212 km; Tajikistan 984 km; Uzbekistan 1,314 km",
                "Laos = Burma 238 km; Cambodia 555 km; China 475 km; Thailand 1,845 km; Vietnam 2,161 km",
                "Latvia = Belarus 161 km; Estonia 333 km; Lithuania 544 km; Russia 332 km",
                "Lebanon = Israel 81 km; Syria 403 km",
                "Lesotho = South Africa 1,106 km",
                "Liberia = Guinea 590 km; Cote d'Ivoire 778 km; Sierra Leone 299 km",
                "Libya = Algeria 989 km; Chad 1,050 km; Egypt 1,115 km; Niger 342 km; Sudan 382 km; Tunisia 461 km",
                "Liechtenstein = Austria 34 km; Switzerland 41 km",
                "Lithuania = Belarus 640 km; Latvia 544 km; Poland 100 km; Russia 261 km",
                "Luxembourg = Belgium 130 km; France 69 km; Germany 128 km",
                "Macau = Mozambique 1,498 km; Tanzania 512 km; Zambia 847 km",
                "Madagascar = ",
                "Malawi = Mozambique 1,498 km; Tanzania 512 km; Zambia 847 km",
                "Malaysia = Brunei 266 km; Indonesia 1,881 km; Thailand 595 km",
                "Maldives = ",
                "Mali = Algeria 1,359 km; Burkina Faso 1,325 km; Cote d'Ivoire 599 km; Guinea 1,062 km; Mauritania 2,236 km; Niger 838 km; Senegal 489 km",
                "Malta = ",
                "Marshall Islands = ",
                "Mauritania = Algeria 460 km; Mali 2,236 km; Morocco 1,564 km; Senegal 742 km",
                "Mauritius = ",
                "Mexico = Belize 276 km; Guatemala 958 km; United States 3,155 km",
                "Micronesia, Federated States of = ",
                "Moldova = Romania 683 km; Ukraine 1202 km",
                "Monaco = France 6 km",
                "Mongolia = China 4,630 km; Russia 3,452 km",
                "Montenegro = Albania 186 km; Bosnia and Herzegovina 242 km; Croatia 19 km; Kosovo 76 km; Serbia 157 km",
                "Montserrat = ",
                "Morocco = Algeria 1,941 km; Mauritania 1,564 km; Spain 8 km",
                "Mozambique = Malawi 1498 km; South Africa 496 km; Eswatini 108 km; Tanzania 840 km; Zambia 439 km; Zimbabwe 1,402 km",
                "Namibia = Angola 1,427 km; Botswana 1,544 km; South Africa 1,005 km; Zambia 244 km",
                "Nauru = ",
                "Navassa Island = ",
                "Nepal = China 1,389 km; India 1,770 km",
                "Netherlands = Belgium 478 km; Germany 575 km",
                "New Caledonia = ",
                "New Zealand = ",
                "Nicaragua = Costa Rica 313 km; Honduras 940 km",
                "Niger = Algeria 951 km; Benin 277 km; Burkina Faso 622 km; Chad 1,196 km; Libya 342 km; Mali 838 km; Nigeria 1,608 km",
                "Nigeria = Benin 809 km; Cameroon 1,975 km; Chad 85 km; Niger 1,608 km",
                "Niue = ",
                "Norfolk Island = ",
                "North Macedonia = Albania 181 km; Bulgaria 162 km; Greece 234 km; Kosovo 160 km; Serbia 101 km",
                "Northern Mariana Islands = ",
                "Norway = Finland 709 km; Sweden 1,666 km; Russia 191 km",
                "Oman = Saudi Arabia 658 km; United Arab Emirates 609 km; Yemen 294 km",
                "Pakistan = Afghanistan 2,670 km; China 438 km; India 3,190 km; Iran 959 km",
                "Palau = ",
                "Panama = Colombia 339 km; Costa Rica 348 km",
                "Papua New Guinea = Indonesia 824 km",
                "Paracel Islands = ",
                "Paraguay = Argentina 2,531 km; Bolivia 753 km; Brazil 1,371 km",
                "Peru = Bolivia 1,212 km; Brazil 2,659 km; Chile 168 km; Colombia 1,494 km; Ecuador 1,529 km",
                "Philippines = ",
                "Pitcairn Islands = ",
                "Poland = Belarus 375 km; Czechia 699 km; Germany 467 km; Lithuania 100 km; Russia 209 km; Slovakia 517 km; Ukraine 498 km",
                "Portugal = Spain 1,224 km",
                "Puerto Rico = ",
                "Qatar = Saudi Arabia 87 km",
                "Romania = Bulgaria 605 km; Hungary 424 km; Moldova 683 km; Serbia 531 km; Ukraine 601 km",
                "Russia = Azerbaijan 338 km; Belarus 1,312 km; China 4,133 km; Estonia 324 km; Finland 1,309 km; Georgia 894 km; Kazakhstan 7,644 km; Korea, North 18 km; Latvia 332 km; Lithuania 261 km; Mongolia 3,452 km; Norway 191 km; Poland 209 km; Ukraine 1,944 km",
                "Rwanda = Burundi 315 km; Congo, Democratic Republic of the 221 km; Tanzania 222 km; Uganda 172 km",
                "Saint Barthelemy = ",
                "Saint Helena, Ascension, and Tristan da Cunha = ",
                "Saint Kitts and Nevis = ",
                "Saint Lucia = ",
                "Saint Martin = ",
                "Saint Pierre and Miquelon = ",
                "Saint Vincent and the Grenadines = ",
                "Samoa = ",
                "San Marino = Italy 37 km",
                "Sao Tome and Principe = ",
                "Saudi Arabia = Iraq 811 km; Jordan 731 km; Kuwait 221 km; Oman 658 km; Qatar 87 km; United Arab Emirates 457 km; Yemen 1,307 km",
                "Senegal = Gambia, The 749 km; Guinea 363 km; Guinea-Bissau 341 km; Mali 489 km; Mauritania 742 km",
                "Serbia = Bosnia and Herzegovina 345 km; Bulgaria 344 km; Croatia 314 km; Hungary 164 km; Kosovo 366 km; North Macedonia 101 km; Montenegro 157 km; Romania 531 km",
                "Seychelles = ",
                "Sierra Leone = Guinea 794 km; Liberia 299 km",
                "Singapore = ",
                "Sint Maarten = Saint Martin 16 km",
                "Slovakia = Austria 105 km; Czechia 241 km; Hungary 627 km; Poland 517 km; Ukraine 97 km",
                "Slovenia = Austria 299 km; Croatia 600 km; Hungary 94 km; Italy 218 km",
                "Solomon Islands = ",
                "Somalia = Djibouti 61 km; Ethiopia 1,640 km; Kenya 684 km",
                "South Africa = Botswana 1,969 km; Lesotho 1,106 km; Mozambique 496 km; Namibia 1,005 km; Eswatini 438 km; Zimbabwe 230 km",
                "South Georgia and South Sandwich Islands = ",
                "South Sudan = Central African Republic 1,055 km; Congo, Democratic Republic of the 714 km; Ethiopia 1,299 km; Kenya 317 km; Sudan 2,158 km; Uganda 475 km",
                "Spain = Andorra 63 km; France 646 km; Gibraltar 1.2 km; Portugal 1,224 km; Morocco 8 km",
                "Spratly Islands = ",
                "Sri Lanka = ",
                "Sudan = Central African Republic 174 km; Chad 1,403 km; Egypt 1,276 km; Eritrea 682 km; Ethiopia 744 km; Libya 382 km; South Sudan 2,158 km",
                "Suriname = Brazil 515 km; Guyana 836 km",
                "Svalbard = ",
                "Sweden = Finland 545 km; Norway 1,666 km",
                "Switzerland = Austria 158 km; France 525 km; Italy 698 km; Liechtenstein 41 km; Germany 348 km",
                "Syria = Iraq 599 km; Israel 83 km; Jordan 379 km; Lebanon 403 km; Turkey (Turkiye) 899 km",
                "Taiwan = ",
                "Tajikistan = Afghanistan 1,357 km; China 477 km; Kyrgyzstan 984 km; Uzbekistan 1,312 km",
                "Tanzania = Burundi 589 km; Congo, Democratic Republic of the 479 km; Kenya 775 km; Malawi 512 km; Mozambique 840 km; Rwanda 222 km; Uganda 391 km; Zambia 353 km",
                "Thailand = Burma 2,416 km; Cambodia 817 km; Laos 1,845 km; Malaysia 595 km",
                "Timor-Leste = Indonesia 253 km",
                "Togo = Benin 651 km; Burkina Faso 131 km; Ghana 1,098 km",
                "Tokelau = ",
                "Tonga = ",
                "Trinidad and Tobago = ",
                "Tunisia = Algeria 1,034 km; Libya 461 km",
                "Turkey (Turkiye) = Armenia 311 km; Azerbaijan 17 km; Bulgaria 223 km; Georgia 273 km; Greece 192 km; Iran 534 km; Iraq 367 km; Syria 899 km",
                "Turkmenistan = Afghanistan 804 km; Iran 1,148 km; Kazakhstan 413 km; Uzbekistan 1,793 km",
                "Turks and Caicos Islands = ",
                "Tuvalu = ",
                "Uganda = Congo, Democratic Republic of the 877 km; Kenya 814 km; Rwanda 172 km; South Sudan 475 km; Tanzania 391 km",
                "Ukraine = Belarus 1,111 km; Hungary 128 km; Moldova 1,202 km; Poland 498 km; Romania 601 km; Russia 1,944 km; Slovakia 97 km",
                "United Arab Emirates = Oman 609 km; Saudi Arabia 457 km",
                "United Kingdom = Ireland 499 km",
                "United States = Canada 8,891 km; Mexico 3,111 km",
                "United States Pacific Island Wildlife Refuges = ",
                "Uruguay = Argentina 541 km; Brazil 1,050 km",
                "Uzbekistan = Afghanistan 144 km; Kazakhstan 2,330 km; Kyrgyzstan 1,314 km; Tajikistan 1,312 km; Turkmenistan 1,793 km",
                "Vanuatu = ",
                "Venezuela = Brazil 2,137 km; Colombia 2,341 km; Guyana 789 km",
                "Vietnam = Cambodia 1,158 km; China 1,297 km; Laos 2,161 km",
                "Virgin Islands = ",
                "Wake Island = ",
                "Wallis and Futuna = ",
                "West Bank = Israel 330 km; Jordan 148 km",
                "Yemen = Oman 294 km; Saudi Arabia 1,307 km",
                "Zambia = Angola 1,065 km; Botswana 0.15 km; Congo, Democratic Republic of the 2,332 km; Malawi 847 km; Mozambique 439 km; Namibia 244 km; Tanzania 353 km; Zimbabwe 763 km",
                "Zimbabwe = Botswana 834 km; Mozambique 1,402 km; South Africa 230 km; Zambia 763 km"};

        String s;
        int n = 0;
        while ( sc.hasNextLine() ) {
            s = sc.nextLine();
            if ( !s.equals( newFile[n] ) ) {
                s = newFile[n];
            }
            line[n] = s; // s is the string, and line is the actual line used
            countries[n++] = s.substring( 0,
                    s.indexOf( '=' )-1 ); // n = the number of countries, left of the = sign
        }
        sc.close();
    }

    /* DOFILE1: processes state_name.tsv and fills in stateNums[][] */
    private static void doFile1( Scanner sc ) {

        // edited state_name.tsv
        String [] newFile = {
                "statenumber	stateid	countryname	start	end",
                "2	USA	United States	1816-01-01	2020-12-31",
                "20	CAN	Canada	1867-07-01	2020-12-31",
                "31	BHM	Bahamas, The	1973-07-10	2020-12-31",
                "40	CUB	Cuba	1902-05-20	2020-12-31",
                "41	HAI	Haiti	1816-01-01	1915-07-04",
                "41	HAI	Haiti	1934-08-15	2020-12-31",
                "42	DOM	Dominican Republic	1844-02-27	2020-12-31",
                "51	JAM	Jamaica	1962-08-06	2020-12-31",
                "52	TRI	Trinidad and Tobago	1962-08-31	2020-12-31",
                "53	BAR	Barbados	1966-11-30	2020-12-31",
                "70	MEX	Mexico	1821-07-01	2020-12-31",
                "80	BLZ	Belize	1981-09-21	2020-12-31",
                "89	UPC	United Provinces of Central America	1823-07-01	1839-12-31",
                "90	GUA	Guatemala	1840-01-01	2020-12-31",
                "91	HON	Honduras	1840-01-01	2020-12-31",
                "92	SAL	El Salvador	1840-01-01	2020-12-31",
                "93	NIC	Nicaragua	1840-01-01	2020-12-31",
                "94	COS	Costa Rica	1840-01-01	2020-12-31",
                "95	PAN	Panama	1903-11-03	2020-12-31",
                "99	GCL	Great Colombia	1821-08-30	1830-09-22",
                "100	COL	Colombia	1830-09-23	2020-12-31",
                "101	VEN	Venezuela	1829-01-01	2020-12-31",
                "110	GUY	Guyana	1966-05-26	2020-12-31",
                "115	SUR	Suriname	1975-11-25	2020-12-31",
                "130	ECU	Ecuador	1830-05-13	2020-12-31",
                "135	PER	Peru	1824-12-09	2020-12-31",
                "140	BRA	Brazil	1822-09-07	2020-12-31",
                "145	BOL	Bolivia	1825-08-06	2020-12-31",
                "150	PAR	Paraguay	1816-01-01	2020-12-31",
                "155	CHL	Chile	1818-04-01	2020-12-31",
                "160	ARG	Argentina	1816-07-09	2020-12-31",
                "165	URU	Uruguay	1830-05-26	2020-12-31",
                "200	UKG	United Kingdom	1816-01-01	2020-12-31",
                "205	IRE	Ireland	1921-12-06	2020-12-31",
                "210	NTH	Netherlands	1816-01-01	2020-12-31",
                "211	BEL	Belgium	1830-10-04	2020-12-31",
                "212	LUX	Luxembourg	1867-05-11	2020-12-31",
                "220	FRN	France	1816-01-01	2020-12-31",
                "225	SWZ	Switzerland	1816-01-01	2020-12-31",
                "230	SPN	Spain	1816-01-01	2020-12-31",
                "235	POR	Portugal	1816-01-01	2020-12-31",
                "240	HAN	Hanover	1816-01-01	1871-01-17",
                "245	BAV	Bavaria	1816-01-01	1871-01-17",
                "255	GMY	Germany	1816-01-01	1945-05-07",
                "260	GFR	German Federal Republic	1949-09-21	2020-12-31",
                "265	GDR	German Democratic Republic	1949-10-05	1990-10-02",
                "267	BAD	Baden	1816-01-01	1871-01-17",
                "269	SAX	Saxony	1816-01-01	1871-01-17",
                "271	WRT	Württemberg	1816-01-01	1871-01-17",
                "273	HSE	Hesse-Kassel (Electoral)	1816-01-01	1871-01-17",
                "275	HSD	Hesse-Darmstadt (Ducal)	1816-01-01	1871-01-17",
                "280	MEC	Mecklenburg-Schwerin	1816-01-01	1871-01-17",
                "290	POL	Poland	1918-11-11	2020-12-31",
                "300	AUH	Austria-Hungary	1816-01-01	1918-11-13",
                "305	AUS	Austria	1918-11-14	2020-12-31",
                "310	HUN	Hungary	1918-11-03	2020-12-31",
                "315	CZE	Czechoslovakia	1918-11-11	1992-12-31",
                "316	CZR	Czech Republic	1993-01-01	2020-12-31",
                "317	SLO	Slovakia	1993-01-01	2020-12-31",
                "325	ITA	Italy	1816-01-01	2020-12-31",
                "327	PAP	Papal States	1816-01-01	1870-09-22",
                "329	SIC	Two Sicilies	1816-01-01	1861-03-16",
                "332	MOD	Modena	1816-01-01	1861-03-16",
                "335	PMA	Parma	1816-01-01	1861-03-16",
                "337	TUS	Tuscany	1816-01-01	1861-03-16",
                "338	MLT	Malta	1964-09-21	2020-12-31",
                "339	ALB	Albania	1913-01-01	2020-12-31",
                "341	MNG	Montenegro	1868-01-01	1915-07-01",
                "341	MNG	Montenegro	2006-06-03	2020-12-31",
                "343	MAC	Macedonia (Former Yugoslav Republic of)	1991-11-20	2020-12-31",
                "344	CRO	Croatia	1992-04-27	2020-12-31",
                "340	SER	Serbia	1878-07-13	1915-10-01",
                "340	SER	Serbia	2006-06-05	2020-12-31",
                "345	YUG	Yugoslavia	1918-12-01	2006-06-04",
                "346	BOS	Bosnia and Herzegovina	1992-04-27	2020-12-31",
                "347	KOS	Kosovo	2008-02-17	2020-12-31",
                "349	SLV	Slovenia	1992-04-27	2020-12-31",
                "350	GRC	Greece	1828-04-25	2020-12-31",
                "352	CYP	Cyprus	1960-08-16	2020-12-31",
                "355	BUL	Bulgaria	1878-03-03	2020-12-31",
                "359	MLD	Moldova	1991-08-27	2020-12-31",
                "360	RUM	Romania	1878-07-13	2020-12-31",
                "365	RUS	Russia	1816-01-01	2020-12-31",
                "366	EST	Estonia	1918-11-11	1940-06-01",
                "366	EST	Estonia	1991-09-06	2020-12-31",
                "367	LAT	Latvia	1918-11-11	1940-06-01",
                "367	LAT	Latvia	1991-09-06	2020-12-31",
                "368	LIT	Lithuania	1918-02-16	1940-06-01",
                "368	LIT	Lithuania	1991-09-06	2020-12-31",
                "369	UKR	Ukraine	1991-12-01	2020-12-31",
                "370	BLR	Belarus	1991-08-25	2020-12-31",
                "371	ARM	Armenia	1991-12-21	2020-12-31",
                "372	GRG	Georgia	1991-12-21	2020-12-31",
                "373	AZE	Azerbaijan	1991-12-21	2020-12-31",
                "375	FIN	Finland	1917-12-06	2020-12-31",
                "380	SWD	Sweden	1816-01-01	2020-12-31",
                "385	NOR	Norway	1905-08-26	2020-12-31",
                "390	DEN	Denmark	1816-01-01	2020-12-31",
                "395	ICE	Iceland	1944-06-17	2020-12-31",
                "402	CAP	Cape Verde	1975-07-05	2020-12-31",
                "404	GNB	Guinea-Bissau	1974-09-10	2020-12-31",
                "411	EQG	Equatorial Guinea	1968-10-12	2020-12-31",
                "420	GAM	Gambia, The	1965-02-18	2020-12-31",
                "432	MLI	Mali	1960-09-22	2020-12-31",
                "433	SEN	Senegal	1960-04-04	2020-12-31",
                "434	BEN	Benin	1960-08-01	2020-12-31",
                "435	MAA	Mauritania	1960-11-28	2020-12-31",
                "436	NIR	Niger	1960-08-03	2020-12-31",
                "437	CDI	Cote d'Ivoire	1960-08-07	2020-12-31",
                "438	GUI	Guinea	1958-10-02	2020-12-31",
                "439	BFO	Burkina Faso	1960-08-05	2020-12-31",
                "450	LBR	Liberia	1847-07-26	2020-12-31",
                "451	SIE	Sierra Leone	1961-04-27	2020-12-31",
                "452	GHA	Ghana	1957-03-06	2020-12-31",
                "461	TOG	Togo	1960-04-27	2020-12-31",
                "471	CAO	Cameroon	1960-01-01	2020-12-31",
                "475	NIG	Nigeria	1960-10-01	2020-12-31",
                "481	GAB	Gabon	1960-08-17	2020-12-31",
                "482	CEN	Central African Republic	1960-08-13	2020-12-31",
                "483	CHA	Chad	1960-08-11	2020-12-31",
                "484	CON	Congo	1960-08-15	2020-12-31",
                "490	DRC	Congo, Democratic Republic of the	1960-06-30	2020-12-31",
                "500	UGA	Uganda	1962-10-09	2020-12-31",
                "501	KEN	Kenya	1963-12-12	2020-12-31",
                "510	TAZ	Tanzania	1961-12-09	2020-12-31",
                "511	ZAN	Zanzibar	1963-12-19	1964-04-26",
                "516	BUI	Burundi	1962-07-01	2020-12-31",
                "517	RWA	Rwanda	1962-01-07	2020-12-31",
                "520	SOM	Somalia	1960-07-01	2020-12-31",
                "522	DJI	Djibouti	1977-06-27	2020-12-31",
                "530	ETH	Ethiopia	1855-02-11	2020-12-31",
                "531	ERI	Eritrea	1993-05-24	2020-12-31",
                "540	ANG	Angola	1975-11-11	2020-12-31",
                "541	MZM	Mozambique	1975-06-25	2020-12-31",
                "551	ZAM	Zambia	1964-10-24	2020-12-31",
                "552	ZIM	Zimbabwe	1965-11-11	2020-12-31",
                "553	MAW	Malawi	1964-07-06	2020-12-31",
                "560	SAF	South Africa	1910-05-31	2020-12-31",
                "563	TRA	Transvaal	1852-01-01	1910-05-30",
                "564	OFS	Orange Free State	1854-03-28	1910-05-30",
                "565	NAM	Namibia	1990-03-21	2020-12-31",
                "570	LES	Lesotho	1966-10-04	2020-12-31",
                "571	BOT	Botswana	1966-09-30	2020-12-31",
                "572	SWA	Swaziland	1968-09-06	2020-12-31",
                "580	MAG	Madagascar (Malagasy)	1816-01-01	1896-08-05",
                "580	MAG	Madagascar	1960-06-26	2020-12-31",
                "581	COM	Comoros	1975-07-06	2020-12-31",
                "590	MAS	Mauritius	1968-03-12	2020-12-31",
                "600	MOR	Morocco	1816-01-01	1904-01-01",
                "600	MOR	Morocco	1956-03-02	2020-12-31",
                "615	ALG	Algeria	1816-01-01	1830-07-05",
                "615	ALG	Algeria	1962-07-05	2020-12-31",
                "616	TUN	Tunisia	1816-01-01	1881-05-12",
                "616	TUN	Tunisia	1956-01-01	2020-12-31",
                "620	LIB	Libya	1816-01-01	1834-12-31",
                "620	LIB	Libya	1951-12-24	2020-12-31",
                "625	SUD	Sudan	1956-01-01	2020-12-31",
                "626	SSD	South Sudan	2011-07-09	2020-12-31",
                "630	IRN	Iran	1816-01-01	2020-12-31",
                "640	TUR	Turkey (Turkiye)	1816-01-01	2020-12-31",
                "645	IRQ	Iraq	1932-10-03	2020-12-31",
                "651	EGY	Egypt	1827-01-01	1855-12-31",
                "651	EGY	Egypt	1922-02-28	2020-12-31",
                "652	SYR	Syria	1946-01-01	2020-12-31",
                "660	LEB	Lebanon	1944-11-22	2020-12-31",
                "663	JOR	Jordan	1946-05-25	2020-12-31",
                "666	ISR	Israel	1948-05-14	2020-12-31",
                "670	SAU	Saudi Arabia	1932-09-23	2020-12-31",
                "678	YEM	Yemen	1918-10-30	2020-12-31",
                "680	YPR	Yemen, People's Republic of	1967-11-30	1990-05-21",
                "690	KUW	Kuwait	1961-06-19	2020-12-31",
                "692	BAH	Bahrain	1971-08-15	2020-12-31",
                "694	QAT	Qatar	1971-09-03	2020-12-31",
                "696	UAE	United Arab Emirates	1971-12-02	2020-12-31",
                "698	OMA	Oman	1816-01-01	2020-12-31",
                "700	AFG	Afghanistan	1816-01-01	1888-12-30",
                "700	AFG	Afghanistan	1919-05-01	2020-12-31",
                "701	TKM	Turkmenistan	1991-10-27	2020-12-31",
                "702	TAJ	Tajikistan	1991-09-09	2020-12-31",
                "703	KYR	Kyrgyzstan	1991-08-31	2020-12-31",
                "704	UZB	Uzbekistan	1991-08-31	2020-12-31",
                "705	KZK	Kazakhstan	1991-12-16	2020-12-31",
                "710	CHN	China	1816-01-01	2020-12-31",
                "711	TBT	Tibet	1913-01-01	1950-10-01",
                "712	MON	Mongolia	1921-03-13	2020-12-31",
                "713	TAW	Taiwan	1949-12-08	2020-12-31",
                "730	KOR	Korea	1816-01-01	1910-08-22",
                "731	PRK	Korea, North	1948-09-09	2020-12-31",
                "732	ROK	Korea, South	1948-08-15	2020-12-31",
                "740	JPN	Japan	1816-01-01	2020-12-31",
                "750	IND	India	1947-08-15	2020-12-31",
                "760	BHU	Bhutan	1949-01-01	2020-12-31",
                "770	PAK	Pakistan	1947-08-14	2020-12-31",
                "771	BNG	Bangladesh	1971-12-16	2020-12-31",
                "775	MYA	Myanmar (Burma)	1816-01-01	1885-12-31",
                "775	MYA	Myanmar (Burma)	1948-01-04	2020-12-31",
                "780	SRI	Sri Lanka	1948-02-04	2020-12-31",
                "781	MAD	Maldives	1965-05-26	2020-12-31",
                "790	NEP	Nepal	1816-01-01	2020-12-31",
                "800	THI	Thailand	1816-01-01	2020-12-31",
                "811	CAM	Cambodia	1953-11-09	2020-12-31",
                "812	LAO	Laos	1954-05-01	2020-12-31",
                "815	VNM	Vietnam (Annam/Cochin China/Tonkin)	1816-01-01	1893-01-01",
                "816	DRV	Vietnam	1954-05-01	2020-12-31",
                "817	RVN	Vietnam, Republic of	1954-05-01	1975-04-30",
                "820	MAL	Malaysia	1957-08-31	2020-12-31",
                "830	SIN	Singapore	1965-08-09	2020-12-31",
                "835	BRU	Brunei	1984-01-01	2020-12-31",
                "840	PHI	Philippines	1946-07-04	2020-12-31",
                "850	INS	Indonesia	1945-08-17	2020-12-31",
                "860	ETM	East Timor	2002-05-20	2020-12-31",
                "900	AUL	Australia	1901-01-01	2020-12-31",
                "910	PNG	Papua New Guinea	1975-09-16	2020-12-31",
                "920	NEW	New Zealand	1907-09-01	2020-12-31",
                "940	SOL	Solomon Islands	1978-07-07	2020-12-31",
                "950	FJI	Fiji	1970-10-10	2020-12-31"};

        String s;
        s = sc.nextLine();		// skip first line, it is the name of the categories
        int n = 0;
        while ( sc.hasNextLine() ) {
            s = sc.nextLine();
            if ( !s.equals( newFile[n] ) ) {
                s = newFile[n];
            }
            IRoadTrip.stateNums[n][0] = getname1( s ); // col0: country name
            // increment n here
            IRoadTrip.stateNums[n++][1] = s.substring( 0,s.indexOf( '\t' ) ); // col1: state number
        }
        sc.close();
    }

    /* DOFILE2: processes capdist.csv by making a temp file and moving blocks around. Completes distances[][] */
    private static void doFile2( Scanner sc,
                                   int [] numA_Arr,int [][] distances ) {

        try {
            FileWriter f = new FileWriter( "newcapd.csv" );
            // moved blocks for more order
            writef( sc,f,3031, 202 );
            writef( sc,f,8888, 606 );
            writef( sc,f,23028, 202 );
            while ( sc.hasNextLine() ) {
                f.write( sc.nextLine()+"\n" );
            }
            f.close();
            File fin = new File( "newcapd.csv" );
            Scanner sc2 = new Scanner( fin );
            String s;
            s = sc2.nextLine();		// skip first line
            // intializing
            int index = 0;
            int n1 = 0; // number of lines
            int n2 = 0;
            while ( sc2.hasNextLine() ) {
                s = sc2.nextLine();
                int j = s.indexOf( ',' );
                int a = Integer.parseInt( s.substring( 0,j ) );
                distances[n2][0] = a;
                if ( index < a ) {
                    index = a;
                    numA_Arr[n1++] = a; // start of block
                }
                s = s.substring( j+1 );
                j = s.indexOf( ',' );
                s = s.substring( j+1 );
                j = s.indexOf( ',' );
                distances[n2][1] = Integer.parseInt( s.
                        substring( 0,j ) );
                s = s.substring( j+1 );
                j = s.indexOf( ',' );
                s = s.substring( j+1 );
                j = s.indexOf( ',' );
                distances[n2++][2] = Integer.parseInt( s.
                        substring( 0,j ) );
            }
            sc2.close();
            fin.delete();

        } catch( Exception e ) {
            System.out.println( "doFile2: ** I/O exception **" );
            System.exit(0 );
        }
    }

    /* WRITEF(): writes corrected capdist.csv information into temp file */
    private static void writef(Scanner sc, FileWriter f,
                               int n1, int n3 ) {

        try {
            FileWriter t = new FileWriter( "temptemp" );
            for ( int i = 0;  i < n1;  i++ ) {
                f.write( sc.nextLine()+"\n" );
            }
            for (int i = 0; i < block; i++ ) {
                t.write( sc.nextLine()+"\n" );
            }
            t.close();
            for ( int i = 0;  i < n3;  i++ ) {
                f.write( sc.nextLine()+"\n" );
            }
            File fin = new File( "temptemp" );
            Scanner sct = new Scanner( fin );
            while ( sct.hasNextLine() ) {
                f.write( sct.nextLine()+"\n" );
            }
            fin.delete();
        } catch( Exception e ) {
            System.out.println( "** writef: I/O exception **" );
            System.exit( 0 );
        }
    } // end of class Trip
} // end of class IRoadTrip
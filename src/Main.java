import java.nio.channels.IllegalChannelGroupException;

import javax.naming.ldap.InitialLdapContext;

class Main extends Program {
/*
    Note that each of these following regions would much rather be classes with their own methods
    We put them all together in the main class to respect the no-objects constraint.
*/

    //#region GLOBAL
        //Global - core operators and tools

        //#region MATHS
        double clamp(double v, double min, double max){
            return Math.max(Math.min(v, max), min);
        }

        double lerp(double a, double b, double i){
            return b*i+a*(1-i);
        }

        double lerp(double a, double b, double i, double weight){
            return lerp(a, b, Math.pow(i, weight));
        }

        double randomLerp(double a, double b){
            double i = random();
            return lerp(a, b, i);
        }

        double randomLerp(double a, double b, double weight){
            double i = random();
            return lerp(a, b, i, weight);
        }

        double randomInRange(double a, double r){
            return lerp(a-r, a+r, random());
        }
        /*
        Some of the math module's functions are used in the program.
        Here are some proposals for implementing these manually, in case it wasn't authorized to use the module.


        MAX AND MIN

        double max(double a, double b){
            T m = b;
            if(a > b){
                m = a;
            }
            return m;
        }

        we could also write

        double max(double a, double b){
            return a>b ? a : b;
        }

        min would be the same function where a>b is replaced by a<b



        CEIL

        double ceil(double v){
            double dec = v%1;
            return v/1 - dec + (dec != 0 ? 1 : 0);
        }


        FLOOR

        double floor(double v){
            return v/1 - v%1;
        }

        which means we could also define Ceil as 
        double ceil(double v){
            return floor(v) + (v%1 != 0 ? 1 : 0);
        }


        POW

        //Note that it doesn't include floating point exponent
        double pow(double a, int e){
            if(p == 0){
                return 1;
            }
            if(p<0){
                return 1/pow(a, -e);
            }
            if(p>0){
                return a*pow(a, e-1);
            }
        }

        
        */
        //#endregion

        //#region VECTOR2
        /*Vector2 - describes points and vectors in a 2D grid.
        
        /!\ CAUTION /!\ note that it is NOT a representation of vector in any PLAN, but a GRID.
        Vectors parameters will always be integers.
        */ 

        Vector2 newVector2(int x, int y){
            Vector2 v = new Vector2();
            v.x = x;
            v.y = y;
            return v;
        }

            //#region Remarkable vectors

            final Vector2 VECTOR2_NULL = newVector2(0, 0);

            final Vector2 VECTOR2_FORWARD = newVector2(1, 0);
            final Vector2 VECTOR2_BACK = newVector2(-1, 0);
            final Vector2 VECTOR2_UP = newVector2(0, 1);
            final Vector2 VECTOR2_DOWN = newVector2(0, -1);

            //#endregion

            //#region Operators

            Vector2 VECTOR2_kProd(Vector2 v, int k){
                return newVector2(k*v.x, k*v.y);
            }

            double VECTOR2_distance(Vector2 a, Vector2 b){
                //Returns the distance between a and b (1-1)
                Vector2 v = VECTOR2_abVector(a, b);

                return Math.sqrt(Math.pow(v.x,2)+Math.pow(v.y,2));
            }

            double VECTOR2_magnitude(Vector2 v){
                //Returns the magnitude of the v vector
                return VECTOR2_distance(VECTOR2_NULL, v);
            }

            Vector2 VECTOR2_abVector(Vector2 a, Vector2 b){
                //Returns the AB vector
                Vector2 v = newVector2(b.x - a.x, b.y - a.y);

                return v;
            }

            double[] VECTOR2_normalized(Vector2 v){
                /*
                As mentionned before, vector in this program are defined in a grid.
                However, it might be helpfull in some cases to know the generic direction of a vector.
                Normalized provides us with a double[] such as [0] is the x paremeter and [1] the y parameter of the normalized vector of v.
                */
                double[] nv = new double[2];
                double m = VECTOR2_magnitude(v);
                nv[0] = v.x/m;
                nv[1] = v.y/m;
            
                return nv;
            }

            //#endregion

        //#endregion
    //#endregion

    //#region RES   | RESOURCES
    
    /* Resources is a key concept of the game.
    They are owned by players and planets.
    Player can exctract resources from planets, and use these for diverse interactions.

    Resoureces are represented as an array of integers
    
    Such as -
    [0] = FE
    [1] = Hydrogen
    [2] = Oxygen
     */

    final int RES_CATEGORIES = 3;
    final int RES_PLAYERINIT_FE = 10;
    final int RES_PLAYERINIT_H = 30;
    final int RES_PLAYERINIT_O = 20;

    int[] newResources(){
        return newResources(-1, -1, -1);
    }

    int[] emptyResources(){
        return newResources(0, 0, 0);
    }

    int[] initResources(){
        return newResources(RES_PLAYERINIT_FE, RES_PLAYERINIT_H, RES_PLAYERINIT_O);
    }

    int[] newResources(int _FE, int _H, int _O){
        int[] res = new int[RES_CATEGORIES];
        res[0] = _FE;
        res[1] = _H;
        res[2] = _O;
        return res;
    }

    //#endregion

    //#region NBS   | NEARBY STATUS

    /* Nearby status represents the current status of a player regarding the system object on his current location.
    It will enable or disable various interactions.

    Nearby status is represented as an array of booleans

    Such as -
    [0] = approached ?
    [1] = landed ?                requires [0]
    [2] = H can be collected ?    requires [0]
    [3] = Fe can be collected ?   requires [1]
    [4] = O can be collected ?    requires [1]
    
     */

    final int NBS_CATEGORIES = 5;

    boolean[] initNBS(){
        boolean[] nbs = new boolean[NBS_CATEGORIES];
        for(int i = 0; i<NBS_CATEGORIES; i ++){
            nbs[i] = false;
        }
        return nbs;
    }
    //#endregion

    //#region PLAYER
    Player initPlayer(String _name, Vector2 _initPos){
        Player p = new Player();
        p.name = _name;
        p.pos = _initPos;
        p.ship = initShip();
        p.ownedResources = initResources();
        p.nearbySOstatus = initNBS();

        return p;
    }

    void PLAYER_move(Player p, Vector2 b){
        int cost = (int)Math.ceil(SHIP_hydrogenRequired(VECTOR2_distance(p.pos, b)));
        println("Le déplacement indiqué coûtera " + cost + " unités d'hydrogène. Continuer ? oui/non");
        if(readString() == "oui"){
            if(cost <= p.ownedResources[1]){
                p.ownedResources[1] -= cost;
                p.pos = b;
            }
            else
            {
                println("Hydrogène insuffisant !");
            }
        }
    }

    void PLAYER_repair(Player p)
    {
        println("Vous disposez de " + p.ownedResources[0] + " unités de Fer.\nCombien en dépenser pour la réparation ? ");
        println("Max : " + Math.min(SHIP_ironRequired(p.ship.MaxHPs-p.ship.HPs),p.ownedResources[0]));
    }
    //#endregion

    //#region SYS   | SYSTEM

    final int DEF_SYS_XDIMENSION = 50;
    final int DEF_SYS_YDIMENSION = 50;

    SysObject[][] SYS_generateSystem(){
        //Default generation of a system


        //Dimensions, contained objects and positions.
        SysObject[][] system = new SysObject[DEF_SYS_YDIMENSION][DEF_SYS_XDIMENSION];
        
        for(int y = 0; y<DEF_SYS_YDIMENSION; y++){
            for(int x = 0; x<DEF_SYS_XDIMENSION; x ++){
                system[y][x] = newSysObject();
            }
        }

        return system;
    }

        //Add here more specific definitions of generateSystem()

    final int XMARGIN = 4;
    final int YMARGIN = 4;

    final int XAXIS_WIDTH = 3;
    final int YAXIS_WIDTH = 3;

    Card SYS_systemToInt(SysObject[][] system, Player p){
        //Returns a string to display which represents a system
        Card card = new Card();
        int range = 1+(int)(p.ship.SightRange * 2);
        int horizRange = range * 2;
        range += YMARGIN*2 + XAXIS_WIDTH;
        horizRange += XMARGIN*4 + YAXIS_WIDTH;
        card.dimension = newVector2(horizRange, range);
        card.lines = new String[range];
        
        int sysFirstX = p.pos.x - XMARGIN - (int)p.ship.SightRange;
        int sysFirstY = p.pos.y - YMARGIN - (int)p.ship.SightRange;

        String line;
        for(int y = 0; y<range-XAXIS_WIDTH; y++){
            line = (int)(y+sysFirstY) + ((y+sysFirstY)/10 < 1 ? " " : "") + "|";
            for(int x = 0; (x+2)<horizRange/2; x ++){
                if(x+sysFirstX == p.pos.x && y+sysFirstY == p.pos.y){
                    line += 'V';
                }
                else if(VECTOR2_distance(p.pos, newVector2(x+sysFirstX, y+sysFirstY)) <= p.ship.SightRange){
                    line += system[y+sysFirstY][x+sysFirstX].type.type.sprite;
                }
                else {
                    line += "x";
                }
                line += " ";
            }
            card.lines[range-y-XAXIS_WIDTH-1] = line;
        }
        for(int y = 1; y<=XAXIS_WIDTH; y++){
            card.lines[range-y] = "  |";
        }

        for(int x = 0; (x+2)<horizRange/2; x ++){
            int currentX = x+sysFirstX;
            card.lines[range-XAXIS_WIDTH] += "--";
            for(int y = XAXIS_WIDTH-2; y>= 0; y--){
                card.lines[range-y-1] += (int)(currentX/Math.pow(10,y)) + " ";
                currentX = currentX % (int)Math.pow(10, y);
            }
        }

        return card;
    }
    //#endregion

    //#region SOT   | OBJECT TYPE
    ObjectType newObjectType(ObjectLabel _label, String _description){
        ObjectType t = new ObjectType();
        t.type = _label;
        t.description = _description;

        return t;
    }
        //#region Generic object types
        final ObjectType SOT_NASO = newObjectType(
            ObjectLabel.Undetermined,
            "Error when importing the object type.");

        final ObjectType SOT_VOID = newObjectType(
            ObjectLabel.Void,
            "Le vide ...");

        final ObjectType SOT_TEL = newObjectType(
            ObjectLabel.Telluric,
            "Une planète tellurique ... ");

        final ObjectType SOT_GAS = newObjectType(
            ObjectLabel.Gasgiant,
            "Une planète gazeuse ...");

        final ObjectType SOT_ICE = newObjectType(
            ObjectLabel.Icegiant,
            "Une géante de glace ...");

        final ObjectType SOT_AST = newObjectType(
            ObjectLabel.Asteroid,
            "Un astéroïde ...");

        final ObjectType SOT_COM = newObjectType(
            ObjectLabel.Comet,
            "Une comète ...");

        final ObjectType SOT_NEB = newObjectType(
            ObjectLabel.Nebula,
            "Une nébuleuse ...");

        final ObjectType SOT_STA = newObjectType(
            ObjectLabel.Star,
            "Une étoile ...");
        //#endregion 

    //#endregion
    
    //#region SO    | SYSTEM OBJECT
    final int SO_CATEGORIES = 9;

    //#region System Object Generation Settings
    final double RES_RNG_RANGE = 0.12;

    SO_GenSettings newGenSettings(double _massMin, double _massMax, double _massAvgw, int[] _resMin, int[] _resMax){
        SO_GenSettings genSet = new SO_GenSettings();
        genSet.MassMin = _massMin;
        genSet.MassMax = _massMax;
        genSet.MassAvgW = _massAvgw;
        genSet.ResMin = _resMin;
        genSet.ResMax = _resMax;

        return genSet;
    }
    //#region Void
    final SO_GenSettings VOID_GENSET = newGenSettings(
        0,
        0,
        0,
        emptyResources(),
        emptyResources()
    );
    //#endregion
    //#region Telluric
    final SO_GenSettings TEL_GENSET = newGenSettings(
        22.6,                   //Base on Kepler-138b
        26,                     //Based on Kepler-10c
        0.55,
        newResources(10, 0, 0),
        newResources(25, 0, 0)
    );
    //#endregion
    //#region Gas giant
    final SO_GenSettings GAS_GENSET = newGenSettings(
        22,                     //Based on Kepler-138d
        27.2,                   //Based on Jupiter
        0.3,
        newResources(0, 10, 0),
        newResources(0, 30, 0)
    );
    //#endregion
    //#region Ice Giant
    final SO_GenSettings ICE_GENSET = newGenSettings(
        -1,
        -1,
        -1,
        newResources(),
        newResources()
    );
        //hydrogen is just the double of oxygen
    //#endregion
    //#region Asteroid
    final SO_GenSettings AST_GENSET = newGenSettings(
        -1,
        -1,
        -1,
        newResources(),
        newResources()
    );
    //#endregion
    //#region Comet
    final SO_GenSettings COM_GENSET = newGenSettings(
        -1,
        -1,
        -1,
        newResources(),
        newResources()
    );
    //#endregion
    //#region Nebula
    final SO_GenSettings NEB_GENSET = newGenSettings(
        -1,
        -1,
        -1,
        newResources(),
        newResources()
    );
    //#endregion
    //#region Star
    final SO_GenSettings STA_GENSET = newGenSettings(
        -1,
        -1,
        -1,
        newResources(),
        newResources()
    );
    //#endregion

//#endregion
    
    //#region System Object Generation
    int[] SO_generateResource(int[] minRes, int[] maxRes, double sizeInd){
        int[] r = new int[RES_CATEGORIES];
        for(int i = 0; i < RES_CATEGORIES; i ++){
            if(maxRes[i] == 0){
                r[i] = 0;
            } else {
                double rng = clamp(randomInRange(sizeInd, RES_RNG_RANGE), 0, 1);
                r[i] = (int)Math.floor(lerp(minRes[i], maxRes[i], rng));
            }
        }
        return r;
    }

    SysObject newSysObject(ObjectType _type, double _mass, int[] _availableResources){
        //Classic generation
        SysObject o = new SysObject();
        o.type = _type;
        o.mass = _mass;
        o.availableResources = _availableResources;

        if(_mass <= 0){
            o.landingCost = -1;
            o.approachRisks = -1;
            o.landingCost = -1;
        }
        else {
            o.landingRisks = SO_computeLandingRisks(_type, _mass);
            o.approachRisks = SO_computeApproachRisks(_type, _mass);
            o.landingCost = SO_computeLandingCost(_type, _mass);
        }
            
        return o;
    }
      
    SysObject newSysObject(ObjectLabel _label){
        //Automatic generation using an object Label
        double rng = random();

        SO_GenSettings genSet = VOID_GENSET;
        ObjectType type = SOT_VOID;

        if(_label == ObjectLabel.Telluric){
            genSet = TEL_GENSET;
            type = SOT_TEL;
        }
        if(_label == ObjectLabel.Gasgiant){
            genSet = GAS_GENSET;
            type = SOT_GAS;
        }
        if(_label == ObjectLabel.Icegiant){
            genSet = ICE_GENSET;
            type = SOT_ICE;
        }
        if(_label == ObjectLabel.Asteroid){
            genSet = AST_GENSET;
            type = SOT_AST;
        }
        if(_label == ObjectLabel.Comet){
            genSet = COM_GENSET;
            type = SOT_COM;
        }
        if(_label == ObjectLabel.Nebula){
            genSet = NEB_GENSET;
            type = SOT_NEB;
        }
        if(_label == ObjectLabel.Star){
            genSet = NEB_GENSET;
            type = SOT_NEB;
        }
        if(_label == ObjectLabel.Void){
            return newVoid();
        }
        if(_label == ObjectLabel.Undetermined) {
            return newNaSO();
        }

        double mass = lerp(genSet.MassMin, genSet.MassMax, rng, genSet.MassAvgW);
        int[] r = SO_generateResource(genSet.ResMin, genSet.ResMax, rng);

        return newSysObject(type, mass, r);
    }
    
    final int GENPROB_VOID = 200;
    final int GENPROB_TEL = 6;
    final int GENPROB_GAS = 6;
    final int GENPROB_ICE = 5;
    final int GENPROB_NEB = 3;
    final int GENPROB_STA = 3;

    final double GENPROB_AV = GENPROB_VOID+GENPROB_TEL+GENPROB_GAS+GENPROB_ICE+GENPROB_NEB+GENPROB_STA;

    SysObject newSysObject(){
        //Random Object generation
        double rng = random();

        double prob = GENPROB_VOID;
        if(rng < prob/GENPROB_AV){
            return newVoid();
        }
        prob += GENPROB_TEL;
        if(rng < prob/GENPROB_AV) {
            return newSysObject(ObjectLabel.Telluric);
        }
        prob += GENPROB_GAS;
        if(rng < prob/GENPROB_AV) {
            return newSysObject(ObjectLabel.Gasgiant);
        }
        prob += GENPROB_ICE;
        if(rng < prob/GENPROB_AV) {
            return newSysObject(ObjectLabel.Icegiant);
        }
        prob += GENPROB_NEB;
        if(rng < prob/GENPROB_AV) {
            return newSysObject(ObjectLabel.Nebula);
        }
        return newSysObject(ObjectLabel.Star);
    }   

    SysObject newNaSO(){
        //Undetermined Object
        return newSysObject(SOT_NASO, -1, newResources());
    }

    SysObject newVoid(){
        //Quick Void generation
        SysObject o = newSysObject(SOT_VOID, 0, emptyResources());
        return o;
    }

    //#endregion

    void SO_interaction(SysObject o, Player p){
        int input = 0;
        while(input != 0){
            //println(HUD_SO_displayInfosCard(o, p) + "\n" + SO_displayAction(o, p));
            input = readInt();
            SO_processAction(o, p, input);
        }
    }

    void SO_processAction(SysObject o, Player p, int input){
        //Process the action

    }

    void SO_extractFE(SysObject o, Player p){
        boolean SO_FeObject =
        o.type.type == ObjectLabel.Telluric ||
        o.type.type == ObjectLabel.Asteroid ||
        o.type.type == ObjectLabel.Star ||
        o.type.type == ObjectLabel.Nebula;


        if(SO_FeObject && p.nearbySOstatus[1]){
            //process
            p.ownedResources[0] = /* processed val*/0;
            //+ a print of the process
        }
    }

    int SO_extractH(SysObject o, Player p){
        boolean SO_HObject =
        o.type.type == ObjectLabel.Star ||
        o.type.type == ObjectLabel.Gasgiant ||
        o.type.type == ObjectLabel.Icegiant ||
        o.type.type == ObjectLabel.Comet ||
        o.type.type == ObjectLabel.Nebula;
        
        return -1;
    }

    int SO_extractO(SysObject o, Player p){
        boolean SO_OObject =
        o.type.type == ObjectLabel.Icegiant ||
        o.type.type == ObjectLabel.Comet ||
        o.type.type == ObjectLabel.Nebula;

        return -1;
    }

    double SO_computeLandingRisks(ObjectType t, double m){
        //Calculate the landing risks according to the object type t.type and mass
        return -1;
    }

    double SO_computeApproachRisks(ObjectType t, double m){
        //Calculate the approach risks according to the object type t.type and mass
        return -1;
    }

    int SO_computeLandingCost(ObjectType t, double m){
        //Compute the landing cost
        return -1;
    }

    /*

    String[] SO_displayAction(SysObject o, Player p){
        //
        return ;
    }

    String SO_typeToHead(SysObject o){
        if()
    }
    */
    //#endregion

    //#region SHIP

    final int SHIP_DEF_INITHPS = 100;
    final double SHIP_DEF_INITSIGHT = 12.5;
    final double SHIP_DEF_INITEFF = 1;

    Ship initShip(){
        return newShip(SHIP_DEF_INITHPS, SHIP_DEF_INITSIGHT, SHIP_DEF_INITEFF);
    }

    Ship newShip(){
        return newShip(0, 0, 0);
    }

    Ship newShip(int _maxHps, double _sightRange, double _efficiency){
        return newShip(_maxHps, _maxHps, _sightRange, _efficiency);
    }

    Ship newShip(int _maxHps, int _currentHps, double _sightRange, double _efficiency){
        Ship s = new Ship();
        s.MaxHPs = _maxHps;
        s.HPs = _currentHps;
        s.SightRange = _sightRange;
        s.Efficiency = _efficiency;
        return s;
    }
    

    int SHIP_hydrogenRequired(double distance){
        //Convert a given distance into a Hydrogen cost
        return -1;
    }

    int SHIP_ironRequired(int HPs){
        //Convert a given HPs amount to a Fe cost
        return -1;
    }

    /* 
    String[] SHIP_displayState(Ship s, int length){
        //Display ship status, line by line
        return;
    }*/

    //#endregion

    //#region HUD   | HEAD UP DISPLAY

        //#region CARD
        Card newCard(String[] lines, Vector2 dim){
            Card card = new Card();
            card.lines = lines;
            //card.dimension = newVector2(length(lines[0]), length(lines));
            card.dimension = dim;

            return card;
        }

        Card newCard(String[] lines, Vector2 dim, boolean resize){
            //if resize is true, returns a newCard with the correct size
            if(resize){
                String[] resizedLines = new String[dim.y];

                int y = 0;
                while(y < length(lines)){
                    String processingLine = lines[y];
                    if(length(processingLine) > dim.x){
                        processingLine = substring(processingLine, 0, dim.x);
                    }
                    for(int c = length(lines[y]); c<dim.x; c++){
                        processingLine += " ";
                    }

                    resizedLines[y] = processingLine;
                    
                    y++;
                }

                String fillingLine = new String(new char[dim.x]).replace("\0", " "); //Could be done with a simple for += too
                while(y < dim.y){
                    resizedLines[y] = fillingLine;
                    y ++;
                }

                return newCard(resizedLines, dim);
            }

            else {
                return newCard(lines, dim);
            }
        }

        final String HORIZ_SEPARATOR = "_"; 
        String HUD_DisplayCardCluster(Card[][] cards){
            //Must be used on a normalized cluster
            String s = "";
            Card[][] normalized = HUD_NormalizeCardCluster(cards);
            //Card[][] normalized = cards;
            String separator = new String(new char[(cards[0][0].dimension.x + length(VERT_SEPARATOR))*length(normalized[0])-length(VERT_SEPARATOR)]).replace("\0", HORIZ_SEPARATOR);
            
            for(int y = 0; y < length(normalized); y ++){
                s += HUD_DisplayCardLine(normalized[y]);
                s += "\n" + separator + "\n\n";
            }

            return s;
        }

        final String VERT_SEPARATOR = " | ";
        String HUD_DisplayCardLine(Card[] cards){
            //Must be used on a normalized line
            String s = "";
            for(int y = 0; y<length(cards[0].lines); y++){
                String line = "";    
                for(int x = 0; x<length(cards); x++){
                    line += cards[x].lines[y] + VERT_SEPARATOR;
                }
                s += substring(line, 0, length(line)-length(VERT_SEPARATOR)) + "\n";
            }

            return s;
        }

        Card[][] HUD_NormalizeCardCluster(Card[][] cards){
            Card[][] normCards = cards;
            int[][] normalizedDim = HUD_GetTileNormalizedDimensions(cards);

            for(int y = 0; y < length(cards); y++){
                for(int x = 0; x < length(cards, 2); x++){
                    normCards[y][x] = newCard(cards[y][x].lines, newVector2(normalizedDim[0][x], normalizedDim[1][y]),true);
                }
            }

            return normCards;
        }

        int[][] HUD_GetTileNormalizedDimensions(Card[][] cards){
            //Normalize every column and lines of the cards cluster to the largests ones
            //return[0] are the max tiles lengths of tile in the x (return[0][x]) column
            //return[1] are the max tiles heights of tile in the y (return[1][y]) line
            
            int[][] lineCol = new int[2][];
            Vector2 clusterDimensions = HUD_GetCardClusterDimension(cards);
            lineCol[1] = new int[clusterDimensions.y];
            lineCol[0] = new int[clusterDimensions.x];
    
            for(int y = 0; y < length(cards); y++){
                for(int x = 0; x < length(cards, 2); x ++){
                    lineCol[0][x] = Math.max(lineCol[0][x], cards[y][x].dimension.x);
                    lineCol[1][y] = Math.max(lineCol[1][y], cards[y][x].dimension.y);
                }
            }
    
            return lineCol; 
        }
    
        Vector2 HUD_GetCardClusterDimension(Card[][] cards){
            Vector2 v = newVector2(0,length(cards));
            for(int y = 0; y<length(cards); y++){
                v.x = Math.max(v.x, length(cards[y]));
            }
    
            return v;
        }
        //#endregion

        //#region SYSTEM OBJECT
        final int HUD_INFOCARD_LENGTH = 20;
        final int HUD_INFOCARD_HEIGHT = 20;

        Card HUD_SO_displayInfosCard(SysObject o, Player p){
            //Display a card giving the available informations and actions on the given object. Line by Line (to help in building the global HUD format)
            String[] lines = new String[HUD_INFOCARD_HEIGHT];
            lines[0] = HUD_SO_displayCardHead(o, p);

            int i = 1;
            while(i<HUD_INFOCARD_HEIGHT && ((i-1)*HUD_INFOCARD_LENGTH) < length(o.type.description)){
                lines[i] = substring(o.type.description, (i-1)*HUD_INFOCARD_LENGTH, Math.min(i*HUD_INFOCARD_LENGTH, length(o.type.description)));
                i++;
            }
        
            return newCard(lines, newVector2(HUD_INFOCARD_LENGTH, HUD_INFOCARD_HEIGHT));
        }

        String HUD_SO_displayCardHead(SysObject o, Player p){
            //Display a card header
            String head = "";
            
            if(p.nearbySOstatus[0]){
                head = "En orbite sur - ";
            } else if(p.nearbySOstatus[1]){
                head = "A la surface de - ";
            } else{
                head = "A proximité de - ";
            }

            if(o.name != null){
                head += o.name + ", ";
            }

            head += o.type.type.displayType;

            return substring(head, 0, Math.min(HUD_INFOCARD_LENGTH, length(o.type.description)));
        }
        //#endregion
    
    //#endregion

    //Object Macro
    //Object Display =
    //          Display infos
    //          What interactions are availabe ?
    //              display these
    //          Call action

    //Object interactions =
    //          Process action code
    //              #1 - Approach
    //              #2 - Land      - 
    //              #3 - Collect H  - always 0 if not gaz/star/..
    //              #4 - Collect Fe - ..........
    //              #5 - Collect O  - ..
    //              #6 - ..

    //              

    



    //Any time -- display the system, ship and player infos

    //1 - Player -moves to a location
    //2 - If location != null -- Display the object card
    //  Ask for action within available ones -- depends on the object type & approach status
    //  Gives the potential results of the action, ask for confirmation
    //  Process the action

    //3 - Else - display void infos, eventually a random tip about the game/universe

    Player player;
    Player player2;
    Player player3;

    void algorithm() {
        println("Entrez votre nom de joueur : ");
        player = initPlayer(readString(), newVector2(20, 20));
        player2 = initPlayer("aa", newVector2(18, 22));
        player3 = initPlayer("aaa", newVector2(32, 14));
        player3.ship.SightRange = 5;
        player2.ship.SightRange = 8.5;
        //HUD_SO_displayInfosCard(newSysObject(newObjectType(ObjectLabel.Tellurique, ""), 20, null), null);
        SysObject[][] system = SYS_generateSystem();
        SysObject[][] system2 = SYS_generateSystem();
        SysObject[][] system3 = SYS_generateSystem();
        Card sysCard = SYS_systemToInt(system, player);
        Card sysCard2 = SYS_systemToInt(system2, player3);
        Card sysCard3 = SYS_systemToInt(system3, player);
        Card sysCard4 = SYS_systemToInt(system3, player2);
        Card[][] hud = new Card[][]{{sysCard, sysCard2},{sysCard4, sysCard3}};
        println(HUD_DisplayCardCluster(hud));
        readInt();
    }
}
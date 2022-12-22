import java.nio.channels.IllegalChannelGroupException;

import javax.naming.ldap.InitialLdapContext;

class Main extends Program {
/*
    Note that each of these following regions would much rather be classes with their own methods
    We put them all together in the main class to respect the no-objects constraint.
*/

    //#region GLOBAL
        //Global - core operators and tools
        boolean Admin(){
            return equals(player.name, "admin");
        }

        boolean confirm(String inp){
            return equals(input, "1") || equals(input, "");
        }

        void waitSeconds(double seconds){
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() < startTime + (seconds * 1000)){}
        }

        boolean isNumeric(String s){
            int i = 0;
            while(i<length(s)-1 && isNumeric(charAt(s, i))){
                i++;
            }
            return isNumeric(charAt(s, i));
        }

        boolean isNumeric(char c){
            return c <= '9' && c >= '0';
        }

        String setAsTableLine(String[] line, int colWidth){
            String s = "";
            for(int i = 0; i<length(line); i ++){
                s += line[i] + new String(new char[Math.max(colWidth-length(line[i]),0)]).replace("\0", " ");
            }

            return s;
        }

        String[] combineArrays(String[][] arrays){
            int len = 0;
            for(int i = 0; i<length(arrays); i++){
                len += length(arrays[i]);
            }
            String[] a = new String[len];
            
            int lines = 0;
    
            for(int i = 0; i<length(arrays); i++){    
                for(int j = 0; j<length(arrays[i]); j++){
                    a[lines] = arrays[i][j];
                    lines ++;
                }
            }
    
            return a;
        }

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
            //Returns a value randomly modified within a given r range
            return lerp(a-r, a+r, random());
        }
        /* Math module explanations
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

    //#region INPUT
    //INPUT regroup function that process inputs into explicit values, or check for the good format of these inputs
    

    boolean INPUT_isValidCoord(String coord){
        int i = 1;
        int sepCount = 0;
        while(i<length(coord)-1 && (isNumeric(charAt(coord, i)) || charAt(coord, i) == ':' || charAt(coord, i) == ';') && sepCount < 2){
            if(charAt(coord, i) == ':' || charAt(coord, i) == ';'){
                sepCount ++;
            }
            i++;
        }

        return length(coord)>=3 && isNumeric(charAt(coord, 0)) && isNumeric(charAt(coord, i)) && sepCount < 2;
    }

    boolean INPUT_isValidCoord(String coord, SysObject[][] sys){
        return INPUT_isValidCoord(coord) && INPUT_inputToVector2(coord).x < length(sys,2) && INPUT_inputToVector2(coord).y < length(sys);
    }

    int INPUT_repairInputToFe(String rep){
        /*Convert a repair input into an Iron value
        -1 means there is an error in the input format
        -2 means it is an incorrect value
        */
        String num = rep;
        int val = 0;
        int mode = 0;

        if(charAt(rep, length(rep)-1) == '%'){
            num = substring(rep, 0, length(rep)-1);
            mode = 1;
        } else if(charAt(rep, 0) == '+'){
            num = substring(rep, 1, length(rep));
            mode = 2;
        }

        if(isNumeric(num)){
            val = Integer.parseInt(num);
        } else {
            return -1;
        }

        if(mode == 0){
            return val;
        } else if (mode == 1){
            if(val < (player.ship.HPs*100/player.ship.MaxHPs)){
                return -2;
            }
            return SHIP_ironRequired((int)((Math.min(val,100)/100.0)*player.ship.MaxHPs)-player.ship.HPs);
        }

        return SHIP_ironRequired(Math.min((int)((val/100.0)*player.ship.MaxHPs),(player.ship.MaxHPs-player.ship.HPs)));
    }

    Vector2 INPUT_inputToVector2(String coord){
        int i = 0;
        while(i<length(coord) && charAt(coord, i) != ':' && charAt(coord, i) != ';'){
            i++;
        }
        return newVector2(Integer.parseInt(substring(coord, 0, i)), Integer.parseInt(substring(coord, i+1, length(coord))));
    }
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

    boolean PLAYER_move(Player p, Vector2 b){
        int cost = (int)Math.ceil(SHIP_hydrogenRequired(VECTOR2_distance(p.pos, b)));

        boolean canMove = cost <= p.ownedResources[1];
        if(canMove){
            p.ownedResources[1] -= cost;
            p.pos = b;
        }
        
        return canMove;
    }

    boolean PLAYER_repair(Player p, int Fe){
        boolean canRepair = Fe <= p.ownedResources[0];
        if(canRepair){
            p.ownedResources[0] -= Fe;
            p.ship.HPs += SHIP_hpsForIron(Fe);
        }

        return canRepair;
    }

    boolean PLAYER_reinforce(Player p, int Fe){
        boolean canReinforce = Fe <= p.ownedResources[0];
        if(canReinforce){
            p.ownedResources[0] -= Fe;
            p.ship.MaxHPs += SHIP_hpsForIron(Fe/4);
            p.ship.HPs += SHIP_hpsForIron(Fe/4);
        }

        return canReinforce;
    }

    boolean PLAYER_upgradeTelescope(Player p, int Fe){
        boolean canUpgrade = Fe <= p.ownedResources[0];
        if(canUpgrade){
            p.ownedResources[0] -= Fe;
            p.ship.SightRange += Fe/SHIP_SIGHTTOIRON;
        }

        return canUpgrade;
    }

    boolean PLAYER_upgradeStorage(Player p, int Fe){
        boolean canUpgrade = Fe <= p.ownedResources[0];
        if(canUpgrade){
            p.ownedResources[0] -= Fe;
            p.ship.Storage += Fe/SHIP_STORAGETOIRON;
        }

        return canUpgrade;
    }
    //#endregion

    //#region SYS   | SYSTEM

    final int DEF_SYS_XDIMENSION = 50;
    final int DEF_SYS_YDIMENSION = 50;

    SysObject[][] SYS_generateSystem(){
        /*Default generation of a system
        dimensions, contained objects and positions.
        */
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

    Card SYS_playerVisionToCard(SysObject[][] system, Player p){
        //Returns a string to display which represents a system
        Card card = new Card();
        int range = 1+(int)(p.ship.SightRange * 2);
        int horizRange = range * 2;
        range += YMARGIN*2 + XAXIS_WIDTH;
        horizRange += XMARGIN*4 + YAXIS_WIDTH;
        card.dimension = newVector2(horizRange, range);
        card.lines = new String[range];
        
        int sysFirstX = Math.max(p.pos.x - XMARGIN - (int)p.ship.SightRange, 0);
        int sysFirstY = Math.max(p.pos.y - YMARGIN - (int)p.ship.SightRange, 0);

        String line;
        for(int y = 0; y<range-XAXIS_WIDTH; y++){
            line = (int)(y+sysFirstY) + ((y+sysFirstY)/10 < 1 ? " " : "") + "|";
            for(int x = 0; (x+2)<horizRange/2; x ++){
                boolean inSysRange = (x+sysFirstX)<length(system, 2) && (y+sysFirstY)<length(system);
                if(x+sysFirstX == p.pos.x && y+sysFirstY == p.pos.y){
                    line += 'V';
                } 
                else if(inSysRange && VECTOR2_distance(p.pos, newVector2(x+sysFirstX, y+sysFirstY)) <= p.ship.SightRange){
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
            genSet = STA_GENSET;
            type = SOT_STA;
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
    
        //#region Generation consts
    final int GENPROB_VOID = 600;
    final int GENPROB_TEL = 6;
    final int GENPROB_GAS = 6;
    final int GENPROB_ICE = 5;
    final int GENPROB_STA = 1;

    final double GENPROB_AV = GENPROB_VOID+GENPROB_TEL+GENPROB_GAS+GENPROB_ICE+GENPROB_STA;
        //#endregion

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
            SO_mainMenu(o, p, input);
        }
    }

    void SO_mainMenu(SysObject o, Player p, int input){
        //Process the action
        /*
        0. Save & Quit
        1. Nouvelle Destination
        2. Nouvelle Observation
        3. Entrer en orbite
        4. Gestion du vaisseau
        (5. Quitter le système)

        1 -
        Entrez les coordonnées de destination. (x:y)
            
            Le déplacement en (x:y) consommera xxxx d'hydrogène.
            Confirmer ? (y/n - oui/non)
                        Move
            
        2 -
        Entrez les coordonnées d'observation. (x:y)


        3 - Set the approached bool to true
            
            0. Quitter l'orbite
            1. Atterrir
            (2. Extraction de fer) // Si étoile
            (3. Extraction d'hydrogène) // Si objet à hydrogène


                1 -
                Le décolage et l'aterrisage consommeront xxxx d'hydrogène
                Confirmer ? (y/n - oui/non)
                    Si landable -

                    0. Décoler
                    1. Extraction de fer
                    (2. Extraction d'oxygène) // Si planète de glace ou comète

                    Sinon -

                    (Si gazeuse) Alors que vous descendez prudemment dans l'épaisse athmosphère de la planète, vous observez tous les compteurs du vaisseau s'affoler.
                    La pression monte en flèche, la température impressionante, vous faîtes vite demi-tour avant de ne subir plus de dommages.
                    Votre vaisseau a subit de lourd dégâts. Il semblerait qu'atterrir sur une géante gazeuse ne soit pas une très bonne idée.

                    (Si étoile) Alors que vous vous approchez de la surface de l'astre flamboyant, vous observez tous les compteurs du vaisseau s'affoler.
                    La température monte en milliers de degrés, et avant que votre vaisseau ne commençent à fondre, vous vous arrachez à la gravité de l'étoile en catastrophe.
                    Votre vaisseau a subit de lourd dégâts. Il semblerait qu'atterrir sur une étoile ne soit pas une très bonne idée.

        */
        

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
    final int SHIP_DEF_INITSTOR = 80;
    final double SHIP_DEF_INITSIGHT = 9.5;
    final double SHIP_DEF_INITEFF = 1;

    Ship initShip(){
        return newShip(SHIP_DEF_INITHPS, SHIP_DEF_INITSIGHT, SHIP_DEF_INITSTOR, SHIP_DEF_INITEFF);
    }

    Ship newShip(){
        return newShip(0, 0, 0, 0);
    }

    Ship newShip(int _maxHps, double _sightRange, int _storage, double _efficiency){
        return newShip(_maxHps, _maxHps, _storage, _sightRange, _efficiency);
    }

    Ship newShip(int _maxHps, int _currentHps, int _storage, double _sightRange, double _efficiency){
        Ship s = new Ship();
        s.MaxHPs = _maxHps;
        s.HPs = _currentHps;
        s.Storage = _storage;
        s.SightRange = _sightRange;
        s.Efficiency = _efficiency;
        return s;
    }

    final int SHIP_SIGHTTOIRON = 8;
    final int SHIP_STORAGETOIRON = 2;
    

    int SHIP_hydrogenRequired(double distance){
        //Convert a given distance into a Hydrogen cost
        return  (int)Math.ceil(Math.pow(distance,0.8)*4);
    }

    int SHIP_ironRequired(int HPs){
        //Convert a given HPs amount to a Fe cost
        return HPs / 2;
    }

    int SHIP_hpsForIron(int Fe){
        return Fe * 2;
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
            /*
            if resize is true, returns a newCard with resized String[] lines according to Vector2 dim
            otherwise, just use the simple constructor with lines and dim.

            It will resize the lines[] array, by -
                  resizing each line so every length(lines[l]) == dim.x
                  adding or deleting lines in the array so length(lines) == dim.y
            
                  Note that added lines are still respecting the dim.x size, and are full of " " characters
            */
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

        Card newCard(){
            return newCard(newVector2(1, 1));
        }

        Card newCard(Vector2 dim){
            String[] lines = new String[dim.y];
            for(int i = 0; i<dim.y; i++){
                lines[i] = new String(new char[dim.x]).replace("\0", " ");
            }

            return newCard(lines, dim);
        }

        final String HORIZ_SEPARATOR = "_"; 
        String HUD_DisplayCardCluster(Card[][] cards){
            //MUST be used on a normalized cluster
            String s = "";
            Card[][] normalized = HUD_NormalizeCardCluster(cards);
            String separator;

            for(int y = 0; y < length(normalized); y ++){
                int sepLen = 0;
                for(int x = 0; x<length(normalized[y]); x++){
                    sepLen += cards[y][x].dimension.x;
                }
                s += HUD_DisplayCardLine(normalized[y]);
                separator = new String(new char[sepLen]).replace("\0", HORIZ_SEPARATOR);
                s += "\n" + separator + "\n\n";
            }

            return s;
        }

        final String VERT_SEPARATOR = " | ";
        String HUD_DisplayCardLine(Card[] cards){
            /* MUST be used on a normalized line
            Display a line of cards.
            
            */
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
            /* Normalize every column and lines of the card cluster to the largests ones so that every card can fit in a grid
            return[0] are the max tiles lengths of tile in the x (return[0][x]) column
            return[1] are the max tiles heights of tile in the y (return[1][y]) line
            */
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
            /* Returns the dimensions of the card cluster. It will take the largest line in the cluster as the x parameter.
             */
            Vector2 v = newVector2(0,length(cards));
            for(int y = 0; y<length(cards); y++){
                v.x = Math.max(v.x, length(cards[y]));
            }
    
            return v;
        }
        //#endregion

        //#region SYSTEM OBJECT
        final int HUD_INFOCARD_LENGTH = 50;
        final int HUD_INFOCARD_HEIGHT = 20;

        Card HUD_SO_displayInfosCard(SysObject o, Player p, boolean select, Vector2 pos){
            //Display a card giving the available informations and actions on the given object. Line by Line (to help in building the global HUD format)
            String[] lines = new String[HUD_INFOCARD_HEIGHT];
            lines[0] = HUD_SO_displayCardHead(o, p, select, pos);

            int i = 1;
            while(i<HUD_INFOCARD_HEIGHT && ((i-1)*HUD_INFOCARD_LENGTH) < length(o.type.description)){
                lines[i] = substring(o.type.description, (i-1)*HUD_INFOCARD_LENGTH, Math.min(i*HUD_INFOCARD_LENGTH, length(o.type.description)));
                i++;
            }
            while(i<HUD_INFOCARD_HEIGHT){
                lines[i] = "";
                i++;
            }
        
            return newCard(lines, newVector2(HUD_INFOCARD_LENGTH, HUD_INFOCARD_HEIGHT), true);
        }

        Card HUD_SO_displayInfosCard(SysObject o, Player p){
            return HUD_SO_displayInfosCard(o, p, false, VECTOR2_NULL);
        }

        String HUD_SO_displayCardHead(SysObject o, Player p, boolean select, Vector2 pos){
            //Display a card header
            String head = "";
            int posx;
            int posy;
            if(select){
                posx = pos.x;
                posy = pos.y;
                head = "Vous observez - ";
            } else {
                posx = p.pos.x;
                posy = p.pos.y;
                if(p.nearbySOstatus[0]){
                    head += "En orbite sur - ";
                } else if(p.nearbySOstatus[1]){
                    head += "A la surface de - ";
                } else{
                    head += "A proximité de - ";
                }
            }

            if(o.name != null){
                head += o.name + ", ";
            }

            head += o.type.type.displayType;
            head += " | x: " + posx + " y: " + posy;

            return substring(head, 0, Math.min(HUD_INFOCARD_LENGTH, length(head)));
        }

        String HUD_SO_displayCardHead(SysObject o, Player p){
            return HUD_SO_displayCardHead(o, p, false, VECTOR2_NULL);
        }
        //#endregion
    
        //#region SYSTEM

        Card HUD_SYS_mainMenu(SysObject o){
            String orbite = "Entrer en orbite";
            if(o.type.type == ObjectLabel.Void){
                orbite = "-";
            }
            String[] actions = new String[]{
                "Save & Quit",
                "Nouvelle Destination",
                "Nouvelle Observation",
                orbite,
                "Gestion du vaisseau",
                "Quitter le système (pas encore implémenté)"
            };
            return HUD_menuCard(actions, "- Entrez le numéros de l'action à faire -");
        }
    
        final int HUD_MENU_MINCALLPOS = 9;
        final int HUD_MENU_LENGTH = 65;
        final String[] ACTIONS_YESNO = new String[]{
            "0. Annuler",
            "1. Confirmer"
        };

        String[] HUD_menuCallAction(String callAction, int prevLen){
            //Returns the call part of menus, with a minimum amount of filling lines
            String[] call = new String[]{"", callAction};
            String[] fill = new String[Math.max(HUD_MENU_MINCALLPOS - prevLen - 1, 0)];
            for(int i = 0; i<length(fill); i++){
                fill[i] = "";
            }
            
            return combineArrays(new String[][]{fill, call});
        }
    
        Card HUD_moveMenu(){
            String[] actions = new String[]{
                "Annuler"
            };
            return HUD_menuCard(actions, "- Entrez les coordonnées de destination (x:y) -");
        }

        Card HUD_confirmMoveMenu(Vector2 dest){
            double distance = VECTOR2_distance(player.pos, dest);
            String call = "- Le voyage en " + dest.x + ":" + dest.y + " consommera " + SHIP_hydrogenRequired(distance) + " unités d'hydrogène -";
            return HUD_menuCard(ACTIONS_YESNO, call, false);
        }

        Card HUD_insufficientHydrogen(int cost){
            String[] lines = new String[]{
                "Hydrogène insuffisant.",
                "Le trajet nécessite " + cost + " unités d'hydrogène.",
                "-",
                "Annulation .."
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }

        Card HUD_insufficientFe(int cost){
            String[] lines = new String[]{
                "Fer insuffisant.",
                "L'action nécessite " + cost + " unités de Fer.",
                "-",
                "Annulation .."
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }

        Card HUD_menuCard(String[] actions, String callAction){
            return HUD_menuCard(actions, callAction, true);
        }

        Card HUD_menuCard(String[] actions, String callAction, boolean generateNumber){
            String[] lines;
            if(generateNumber){
                lines = combineArrays(new String[][]{HUD_numberList(actions), HUD_menuCallAction(callAction, length(actions))});
            } else {
                lines = combineArrays(new String[][]{actions, HUD_menuCallAction(callAction, length(actions))});
            }
            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }


        //#region SHIP management
        Card HUD_shipManagementMenu(){
            String[] actions = new String[]{
                "Annuler",
                "Réparer la coque",
                "Renforcer la coque",
                "Améliorer le téléscope",
                "Améliorer le stockage"
            };

            return HUD_menuCard(actions, "- Entrez le numéros de l'action à faire -");
        }

            //#region Repair

        Card HUD_shipRepairMenu(){
            String[] lines = new String[]{
                " Vous pouvez indiquer le montant de fer à dépenser,",
                " le pourcentage à restaurer, ou le pourcentage de restauration.",
                "",
                " Exemple :",
                "20  - utilise 20 de fer",
                "75% - restaure la coque à 75%",
                "+5  - ajoute 5% à l'état de la coque",
                "",
                "",
                "- Entrez la valeur de réparation souhaitée -"
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }

        Card HUD_shipRepairConfirmMenu(int Fe, int rep){
            return HUD_menuCard(ACTIONS_YESNO, "- Vous consommerez " + Fe + "u. de fer pour restaurer la coque à " + rep + "% -", false);
        }

            //#endregion

            //#region Reinforce
        Card HUD_shipReinforceMenu(){
            String[] lines = new String[]{
                " Renforcer la coque permet d'augmenter sa durée de vie.",
                " Il faudra cependant plus de fer pour la réparer.",
                "",
                "",
                "- Indiquez le montant de fer à utiliser -"
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }

        Card HUD_shipReinforceConfirmMenu(){
            return HUD_menuCard(ACTIONS_YESNO, "- Confirmer ? -", false);
        }
            //#endregion

            //#region Upgrade Telescope
        Card HUD_shipTelescopeUpgradeMenu(){
            String[] lines = new String[]{
                " Améliorer le télescope permet d'améliorer la portée d'observation.",
                " Améliorer la portée d'1 unité de système demande " + SHIP_SIGHTTOIRON + " de Fer.",
                "",
                "",
                "- Indiquez le nombre d'amélioration à acheter. 1 = " + SHIP_SIGHTTOIRON + " Fe. -"
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }

        Card HUD_shipTelescopeUpgradeConfirmMenu(){
            return HUD_menuCard(ACTIONS_YESNO, "- Confirmer ? -", false);
        }
            //#endregion

            //#region Upgrade Storage
        Card HUD_shipStorageUpgradeMenu(){
            String[] lines = new String[]{
                " Améliorer la capacité de stockage permet de transporter plus de ressources.",
                " 1 espace de stockage supplémentaire coûte " + SHIP_STORAGETOIRON + " de Fer.",
                "",
                "",
                "- Indiquez la quantité d'espace stockage à augmenter -"
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }

        Card HUD_shipStorageUpgradeConfirmMenu(){
            return HUD_menuCard(ACTIONS_YESNO, "- Confirmer ? -", false);
        }
            //#endregion
        //#endregion

        final int STATUS_COLWIDTH = 15;
        final String STATUS_UNIT = " u";

        Card HUD_Status(int[] resPreview, int hpPreview, int storePreview){
            int totalPrev = 0;
            int totalRes = 0;
            String store = "";
            String total = "";
            String hp = "";

            String[] res = new String[]{
                player.ownedResources[0]+"",
                player.ownedResources[1]+"",
                player.ownedResources[2]+""
            };

            //#region Preview
            for(int i = 0; i<length(res); i++){
                totalPrev += resPreview[i];
                totalRes += player.ownedResources[i];
                if(resPreview[i] != 0){
                    res[i] = player.ownedResources[i] - resPreview[i] + "(-" + resPreview[i] + ")";
                }
            }

            if(totalPrev != 0){
                total = totalRes-totalPrev + "(-" + totalPrev + ")";
            } else {
                total = totalRes + "";
            }

            if(hpPreview != 0){
                hp = (((player.ship.HPs+hpPreview)*100)/player.ship.MaxHPs) + "(+" + hpPreview + ")";
            } else {
                hp = player.ship.HPs*100/player.ship.MaxHPs + "";
            }

            if(storePreview != 0){
                store = player.ship.Storage+storePreview + "(+" + storePreview + ")";
            } else {
                store = player.ship.Storage+"";
            }
            //#endregion

            String[] lines = new String[]{
                "Resources -",
                setAsTableLine(new String[]{"Fer :", res[0], STATUS_UNIT}, STATUS_COLWIDTH),
                setAsTableLine(new String[]{"Hydrogène :", res[1], STATUS_UNIT}, STATUS_COLWIDTH),
                setAsTableLine(new String[]{"Oxygène :", res[2], STATUS_UNIT}, STATUS_COLWIDTH),
                "",
                "Espace de stockage : " + total + "/" + store,
                setAsTableLine(new String[]{"____","____","____"}, STATUS_COLWIDTH),
                "",
                "Vaisseau - ",
                "Etat de la coque   :  " + hp + " %"
            };

            return newCard(lines, newVector2(50, length(lines)), true);
        }

        Card HUD_Status(){
            return HUD_Status(emptyResources(), 0, 0);
        }

        Card HUD_Status(int[] preview){
            return HUD_Status(preview, 0, 0);
        }
    
        String[] HUD_numberList(String [] list){
            for(int i = 0; i<length(list); i++){
                list[i] = i + ". " + list[i];
            }
            return list;
        }

        //#endregion
    //#endregion

    //#region GMP   | GAMEPLAY
    void GMP_insufficientFE(int amount){
        win_botLeft = HUD_insufficientFe(amount);
        GMP_displayHUD();
        waitSeconds(TEMPWINDOW);
    }

    void GMP_system(){
        win_topLeft = SYS_playerVisionToCard(sys_current, player);
        win_topRight = HUD_SO_displayInfosCard(sys_current[player.pos.y][player.pos.x], player);
        win_botLeft = HUD_SYS_mainMenu(sys_current[player.pos.y][player.pos.x]);
        win_botRight = HUD_Status();

        GMP_displayHUD();
        println(logCache);
        logCache = "";

        input = readString();
        if(equals(input,"1")){
            GMP_move();
        } else if(equals(input, "4")){
            GMP_ManageShip();
        } else if(Admin()){
            GMP_adminConsole(input.split(" "));
        }
        GMP_system();
    }

    void GMP_adminConsole(String[] params){
        if(equals(params[0], "log")) {
            if(equals(params[1],"maxhps")){
                logCache = ""+player.ship.MaxHPs;
            } else if(equals(params[1], "hps")){
                logCache = ""+player.ship.HPs;
            } else if(equals(params[1], "sight")){
                logCache = ""+player.ship.SightRange;
            }
        } else if(equals(params[0], "set")){
            if(isNumeric(params[2])){
                int setVal = Integer.parseInt(params[2]);
                if(equals(params[1], "fe")){
                    player.ownedResources[0] = setVal;
                    logCache = "Iron";
                } else if(equals(params[1], "h")){
                    player.ownedResources[1] = setVal;
                    logCache = "Hydrogen";
                } else if(equals(params[1], "o")){
                    player.ownedResources[2] = setVal;
                    logCache = "Oxygen";
                } else if(equals(params[1], "sight")){
                    player.ship.SightRange = setVal;
                    logCache = "Sight";
                }
            } else if(INPUT_isValidCoord(params[2], sys_current)){
                Vector2 coord = INPUT_inputToVector2(params[2]);
                if(equals(params[1], "pPos")){
                    player.pos = coord;
                    logCache = "Player position";
                }
            }
            if(equals(logCache, "")){
                logCache = "Error in the input";
            } else{
                logCache = "Successfully set " + logCache;
            }
            
        }
    }

    final double TEMPWINDOW = 1.8;
    void GMP_move(){
        win_botLeft = HUD_moveMenu();

        GMP_displayHUD();

        input = readString();

        if(INPUT_isValidCoord(input, sys_current)){
            Vector2 inputCoord = INPUT_inputToVector2(input);
            int cost = SHIP_hydrogenRequired(VECTOR2_distance(player.pos, inputCoord));
            win_botLeft = HUD_confirmMoveMenu(inputCoord);
            win_botRight = HUD_Status(new int[]{0, cost, 0});

            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_move(player, inputCoord)){
                    win_botLeft = HUD_insufficientHydrogen(cost);
                    GMP_displayHUD();
                    waitSeconds(TEMPWINDOW);
                }
            }
        }
        GMP_system();
    }

    //#region Ship Management
    void GMP_ManageShip(){
        win_botLeft = HUD_shipManagementMenu();

        GMP_displayHUD();

        input = readString();

        if(equals(input, "1")){
            //REPAIR
            GMP_shipRepair();    
        } else if(equals(input, "2")){
            //REINFORCE
            GMP_shipReinforce();
        } else if(equals(input, "3")){
            //TELESCOPE UPGRADE
            GMP_shipTelescopeUpgrade();
        } else if(equals(input, "4")){
            //STORAGE UPGRADE
            GMP_shipStorageUpgrade();
        }
        GMP_system();
    }

    void GMP_shipRepair(){
        win_botLeft = HUD_shipRepairMenu();
        GMP_displayHUD();

        input = readString();
        int feAmount = INPUT_repairInputToFe(input);
        println(feAmount);
        int repairPerc = ((SHIP_hpsForIron(feAmount) + player.ship.HPs)*100)/player.ship.MaxHPs;
        if(feAmount>0){
            win_botLeft = HUD_shipRepairConfirmMenu(feAmount, repairPerc);
            win_botRight = HUD_Status(new int[]{feAmount, 0, 0}, SHIP_hpsForIron(feAmount), 0);

            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_repair(player, feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        }
    }

    void GMP_shipReinforce(){
        win_botLeft = HUD_shipReinforceMenu();
        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int feAmount = Integer.parseInt(input);
            win_botLeft = HUD_shipReinforceConfirmMenu();
            win_botRight = HUD_Status(new int[]{feAmount, 0, 0});
            GMP_displayHUD();

            input = readString();

            if(confirm(input)){    
                if(!PLAYER_reinforce(player, feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        }
    }

    void GMP_shipTelescopeUpgrade(){
        win_botLeft = HUD_shipTelescopeUpgradeMenu();
        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int feAmount = Integer.parseInt(input)*SHIP_SIGHTTOIRON;
            win_botLeft = HUD_shipTelescopeUpgradeConfirmMenu();
            win_botRight = HUD_Status(new int[]{feAmount, 0, 0});
            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_upgradeTelescope(player, feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        }
    }

    void GMP_shipStorageUpgrade(){
        win_botLeft = HUD_shipStorageUpgradeMenu();
        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int feAmount = Integer.parseInt(input)*SHIP_STORAGETOIRON;
            win_botLeft = HUD_shipStorageUpgradeConfirmMenu();
            win_botRight = HUD_Status(new int[]{feAmount, 0, 0}, 0, Integer.parseInt(input));
            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_upgradeStorage(player,feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        }
    }
    //#endregion

    void GMP_displayHUD(){
        println(HUD_DisplayCardCluster(new Card[][]{{win_topLeft, win_topRight},{win_botLeft, win_botRight}}));
    }

    //#endregion
    /*
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
    */
    String input;

    Player player;
    Player player2;
    Player player3;

    Card win_topLeft;
    Card win_topRight;
    Card win_botLeft;
    Card win_botRight;

    String logCache = "";

    SysObject[][] sys_current;

    void algorithm() {
        println("Entrez votre nom de joueur aa: ");
        player = initPlayer(readString(), newVector2(20, 16));
        player.ship.HPs = 50;
        sys_current = SYS_generateSystem();
        /*
        player2 = initPlayer("aa", newVector2(18, 22));
        player3 = initPlayer("aaa", newVector2(32, 14));
        player3.ship.SightRange = 5;
        player2.ship.SightRange = 8.5;
        //HUD_SO_displayInfosCard(newSysObject(newObjectType(ObjectLabel.Tellurique, ""), 20, null), null);
        SysObject[][] system = SYS_generateSystem();
        SysObject[][] system2 = SYS_generateSystem();
        SysObject[][] system3 = SYS_generateSystem();
        Card sysCard = SYS_playerVisionToCard(system, player);
        Card sysCard2 = SYS_playerVisionToCard(system2, player3);
        Card sysCard3 = SYS_playerVisionToCard(system3, player);
        Card sysCard4 = SYS_playerVisionToCard(system3, player2);
        Card surrounding = HUD_SO_displayInfosCard(system[player.pos.y][player.pos.y], player);

        Card Actions = newCard(new String[]{""," Sélectionnez les coordonnées d'observation."}, newVector2(50, 15), true);
        Card ac = HUD_SYS_mainMenu(system[player.pos.y][player.pos.x]);

        Card[][] hud = new Card[][]{{sysCard, surrounding},{ac, newCard()}};
        println(HUD_DisplayCardCluster(hud));
        int xSelect = readInt();
        int ySelect = readInt();
        Card info = HUD_SO_displayInfosCard(system[ySelect][xSelect], player, true, newVector2(xSelect, ySelect));

        hud = new Card[][]{{sysCard, surrounding},{ac, info}};
        //for(int i = 0; i < length(info.lines); i++){
        //    println(info.lines[i]);
        //}
        //hud = new Card[][]{{HUD_SO_displayInfosCard(system[ySelect][xSelect], player)}};
        println(HUD_DisplayCardCluster(hud));*/
        GMP_system();
        readInt();
    }
}
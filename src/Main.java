import java.io.ObjectStreamField;
import java.nio.channels.IllegalChannelGroupException;

import javax.naming.ldap.InitialLdapContext;

import extensions.CSVFile;
import extensions.File;

class Main extends Program {
/*
    Note that each of these following regions would much rather be classes with their own methods
    We put them all together in the main class to respect the no-objects constraint.
*/

    //#region GLOBAL
        //Global - core operators and tools
        String hoursToString(int time){
            String s = "";
            int initTime = time;
            if(time > 8760){
                s += time/8760 + " années, ";
                time %= 8760;
            }
            if(time > 24){
                s += time/24 + " jours, ";
                time %= 24;
            }
            if(time > 0 || initTime == 0){
                s += time + " heures, ";
            }
            s = substring(s, 0, length(s)-2) + ".";
            return s; 
        }

        boolean confirm(String input){
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
            return length(s)>0 && isNumeric(charAt(s, i));
        }

        boolean isNumeric(char c){
            return c <= '9' && c >= '0';
        }

        final String[] COMMAND_PREFIXES = new String[]{
            "log",
            "set",
            "gen"
        };

        boolean isCommand(String s){
            return isIn(COMMAND_PREFIXES, s.split(" ")[0]);
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

        String[] enlargeArray(String[] array, int newTiles){
            int len = length(array);
            String[] greaterArray = new String[len + newTiles];
            for(int i = 0; i<len; i++){
                greaterArray[i] = array[i];
            }
            return greaterArray;
        }

        boolean isIn(ObjectLabel[] tab, ObjectLabel e){
            int i = 0;
            while(i<length(tab)-1 && tab[i] != e){
                i++;
            }
            return length(tab)>0 && tab[i] == e;
        }

        boolean isIn(String[] tab, String e){
            int i = 0;
            while(i<length(tab)-1 && !equals(tab[i], e)){
                i++;
            }
            return length(tab)>0 && equals(tab[i], e);
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

        int intDivToCeil(int a, int b){
            return (int)Math.ceil(a*1.0/b*1.0);
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

        if(length(rep)<1){
            return -1;
        } else if(charAt(rep, length(rep)-1) == '%'){
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
            return Math.min(val, SHIP_ironRequired(player.ship.MaxHPs - player.ship.HPs));
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

    int totalRes(int[] res){
        int tot = 0;
        for(int i = 0; i<3; i++){
            tot += res[i];
        }

        return tot;
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

    void PLAYER_consumeHydrogen(Player p, int amount){
        p.ownedResources[1] -= amount;
        if(p.ownedResources[1] <= 0){
            GMP_endGame(1);
        }
    }

    boolean PLAYER_move(Player p, Vector2 b){
        double distance = VECTOR2_distance(p.pos, b);
        int cost = SHIP_hydrogenRequired(distance);

        boolean canMove = cost <= p.ownedResources[1];
        if(canMove){
            PLAYER_consumeHydrogen(p, cost);
            GMP_spendTime((int)Math.ceil(distance/p.ship.Speed));
            p.pos = b;
            sys_current[b.y][b.x].visited = true;
        }
        
        return canMove;
    }

    boolean PLAYER_approach(Player p){
        boolean canApproach = p.ownedResources[1] >= APPROACH_COST;
        if(canApproach){
            PLAYER_consumeHydrogen(p, APPROACH_COST);
            p.nearbySOstatus[0] = true;
            sys_current[p.pos.y][p.pos.x].approached = true;
        }
        return canApproach;
    }

    boolean PLAYER_land(Player p, SysObject o){
        int cost = o.landingCost;

        boolean canLand = cost <= p.ownedResources[1];
        if(canLand){
            PLAYER_consumeHydrogen(p, cost);
            p.nearbySOstatus[0] = false;
            p.nearbySOstatus[1] = true;
        }
        return canLand;
    }

    boolean PLAYER_extract(Player p, SysObject o, int resIdx, int res){
        int cost = SO_HcostForResource(o, resIdx, res);

        boolean canExtract = cost <= p.ownedResources[1];
        if(canExtract){
            GMP_spendTime(intDivToCeil(cost, 4)+1);
            PLAYER_consumeHydrogen(p, cost);
            if(o.type.type == ObjectLabel.Star){
                if(!SHIP_InflictDamage(p.ship, cost*2 + 1)){
                    GMP_endGame(2);
                }
            }
            p.ownedResources[resIdx] += res;
            o.availableResources[resIdx] -= res;
        }

        return canExtract;
    }

    boolean PLAYER_consumeOxygen(Player p, int cost){
        boolean hasEnough = cost < p.ownedResources[2];

        if(hasEnough){
            p.ownedResources[2] -= cost;
        }

        return hasEnough;
    }

    boolean PLAYER_repair(Player p, int Fe){
        boolean canRepair = Fe <= p.ownedResources[0];
        if(canRepair){
            int repairAmount = SHIP_hpsForIron(Fe);
            GMP_spendTime(intDivToCeil(repairAmount, 10));
            p.ownedResources[0] -= Fe;
            p.ship.HPs += repairAmount;
        }

        return canRepair;
    }

    boolean PLAYER_reinforce(Player p, int Fe){
        boolean canReinforce = Fe <= p.ownedResources[0];
        if(canReinforce){
            int reinforceAmount = SHIP_hpsForIron(Fe/4);
            GMP_spendTime(intDivToCeil(reinforceAmount, 2));
            p.ownedResources[0] -= Fe;
            p.ship.MaxHPs += reinforceAmount;
            p.ship.HPs += reinforceAmount;
        }

        return canReinforce;
    }

    boolean PLAYER_upgradeTelescope(Player p, int Fe){
        boolean canUpgrade = Fe <= p.ownedResources[0];
        if(canUpgrade){
            GMP_spendTime(Fe/SHIP_SIGHTTOIRON);
            p.ownedResources[0] -= Fe;
            p.ship.SightRange += Fe/SHIP_SIGHTTOIRON;
        }

        return canUpgrade;
    }

    boolean PLAYER_upgradeStorage(Player p, int Fe){
        boolean canUpgrade = Fe <= p.ownedResources[0];
        if(canUpgrade){
            GMP_spendTime(intDivToCeil(Fe/SHIP_STORAGETOIRON, 5));
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
                    line += "?";
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
            "Le vide est une notion abstraite, on parle 'd'ultravide' quand la densité"
            + " de matière est très faible, c'est à dire qu'il n'y a 'presque' rien."
            + " C'est cet ultra vide que l'on retrouve dans l'espace, le vide sidéral."
            + " En réalité, même la matière, ta main, un verre, une table, sont constitués de vide à plus de 99% !");

        final ObjectType SOT_TEL = newObjectType(
            ObjectLabel.Telluric,
            "Une planète tellurique est une planète constitué majoritairement de roches et de métaux."
            + " Elles sont très denses, ont une surface solide et souvent peu de satelites."
            + " -"
            + " Dans le système solaire, on peut citer par exemple :"
            + " Mercure, Vénus, Mars, et la Terre !"
            + " La lune n'est pas une planète, c'est un satellite, en revanche c'est également un corps tellurique, comme Io et Europe, deux satellites (ou 'lune') de Jupiter.");

        final ObjectType SOT_GAS = newObjectType(
            ObjectLabel.Gasgiant,
            "Une géante gazeuse est une planète constitué majoritairement de gaz hydrogène et d'helium, très volumineuses mais de faible densité."
            + " Contrairement à ce que l'on peut parfois penser, saturne et ses anneaux n'est pas une exception !"
            + " Il est très commun pour une planète gazeuse d'arborer des anneaux, mais ils sont parfois moins perceptibles. -"
            + " Dans le système solaire, on peut citer comme planètes gazeuses : Jupiter, Saturne, Uranus et Neptune.");

        final ObjectType SOT_ICE = newObjectType(
            ObjectLabel.Icegiant,
            "Les planètes et lunes de glaces sont des corps céleste majoritairement composés de méthane, d'amoniac ou d'EAU !"
            + " Les lunes de glaces en particuliers suscitent beaucoup l'intérêt des scientifiques ces dernières années."
            + " Encélades, une petite lune de Saturne, est géologiquement active, et pourrait abriter un océan d'eau liquide sous sa croûte de glace."
            + " Un espoir de peut-être y trouver de la vie extra-terrestre ! -"
            + " Dans notre système solaire, on peut citer en géante de glaces Neptune et Uranus, (aussi géantes gazeuse). -"
            + " Comme lune de glaces, on peut citer Encélade, lune de Saturne, ainsi que Europe, Ganymède, Callisto et Io, lunes de Jupiter.");

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
            "Une étoile est un objet très massif, qui émet de la lumière par 'réactions nucléaire', ou qui était dans cet état plus tôt dans son cycle de vie."
            + " Ce sont parmis les objets les plus grands de l'univers, et c'est autour d'eux que s'organisent des systèmes stellaire, comme le notre, le système solaire."
            + " Notre système s'appelle système 'Solaire' car les objets de ce système, comme la Terre, gravitent autour du 'Soleil' !");
        //#endregion 

    //#endregion
    
    //#region SO    | SYSTEM OBJECT
    final int SO_CATEGORIES = 9;

    //#region System Object Generation Settings
    final double RES_RNG_RANGE = 0.12;

    ObjectLabel[] unlandable = new ObjectLabel[]{ObjectLabel.Gasgiant, ObjectLabel.Star};

    SO_GenSettings newGenSettings(double _massMin, double _massMax, double _massAvgw, int[] _resMin, int[] _resMax){
        SO_GenSettings genSet = new SO_GenSettings();
        genSet.MassMin = _massMin;
        genSet.MassMax = _massMax;
        genSet.MassAvgW = _massAvgw;    //will be used in the generation of a random mass to tweak the averrage, see https://www.desmos.com/calculator/pi2bmjrmfi?lang=fr
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
        15,                         //22.6 Base on Kepler-138b, 15 based on Deimos as a moon
        26,                         //Based on Kepler-10c
        0.35,                      //Gives an average around 23.5
        newResources(6, 0, 0),
        newResources(25, 0, 0)
    );
    //#endregion
    //#region Gas giant
    final SO_GenSettings GAS_GENSET = newGenSettings(
        22,                     //Based on Kepler-138d
        27.2,                   //Based on Jupiter
        0.3,
        newResources(0, 25, 0),
        newResources(0, 60, 0)
    );
    //#endregion
    //#region Ice Giant
    final SO_GenSettings ICE_GENSET = newGenSettings(
        15,
        26,
        0.35,
        newResources(1, 16, 8),
        newResources(5, 40, 20)
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
        29,
        32,
        1.5,
        newResources(20, 40, 0),
        newResources(60, 120, 0)
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
        o.baseResources = new int[3];
        for(int i = 0; i<3; i++){
            o.baseResources[i] = _availableResources[i];
        }

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

    double SO_computeLandingRisks(ObjectType t, double m){
        //Calculate the landing risks according to the object type t.type and mass
        return -1;
    }

    double SO_computeApproachRisks(ObjectType t, double m){
        //Calculate the approach risks according to the object type t.type and mass
        return -1;
    }

    int SO_computeLandingCost(ObjectType t, double m){
        //Gives the landing cost of an object https://www.desmos.com/calculator/ucxra9uldq
        return (int)Math.ceil(Math.pow(m/12.0, 4));
    }

    //#endregion

    //#region SHIP

    final int SHIP_DEF_INITHPS = 100;
    final int SHIP_DEF_INITSTOR = 80;
    final double SHIP_DEF_INITSIGHT = 9.5;
    final double SHIP_DEF_INITEFF = 1;
    final double SHIP_DEF_INITSPEED = 0.5;

    Ship initShip(){
        return newShip(SHIP_DEF_INITHPS, SHIP_DEF_INITSIGHT, SHIP_DEF_INITSTOR, SHIP_DEF_INITEFF, SHIP_DEF_INITSPEED);
    }

    Ship newShip(){
        return newShip(0, 0, 0, 0, 0);
    }

    Ship newShip(int _maxHps, double _sightRange, int _storage, double _efficiency, double _speed){
        return newShip(_maxHps, _maxHps, _storage, _sightRange, _efficiency, _speed);
    }

    Ship newShip(int _maxHps, int _currentHps, int _storage, double _sightRange, double _efficiency, double _speed){
        Ship s = new Ship();
        s.MaxHPs = _maxHps;
        s.HPs = _currentHps;
        s.Storage = _storage;
        s.SightRange = _sightRange;
        s.Efficiency = _efficiency;
        s.Speed = _speed;
        return s;
    }

    final int SHIP_SIGHTTOIRON = 8;
    final int SHIP_STORAGETOIRON = 2;
    

    int SHIP_hydrogenRequired(double distance){
        //Convert a given distance into a Hydrogen cost
        return  (int)Math.ceil(Math.pow(distance,0.8)*3);
    }

    int SHIP_ironRequired(int HPs){
        //Convert a given HPs amount to a Fe cost
        return HPs / 2;
    }

    int SHIP_hpsForIron(int Fe){
        return Fe * 2;
    }

    boolean SHIP_InflictDamage(Ship s, int damage){
        s.HPs -= damage;
        return s.HPs > 0;
    }

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

        Card newCard(String[] lines, Vector2 dim, boolean resize, boolean cut){
            if(!cut){
                String[] resizedLines = new String[dim.y];
                int y = 0;
                int added = 0;
                while(y+added<length(lines)){
                    String processingLine = lines[y];
                    if(length(processingLine) > dim.x){
                        processingLine = substring(processingLine, 0, dim.x);
                    }
                    for(int c = length(lines[y]); c<dim.x; c++){
                        processingLine += " ";
                    }
                    resizedLines[y+added] = processingLine;

                    int toAdd = (length(lines[y]))/dim.x;
                    int currentAdd = 1;
                    if(toAdd > 0){
                        while(currentAdd<=toAdd){
                            added ++;
                            resizedLines[y+added] = substring(lines[y],currentAdd*dim.x,Math.min(currentAdd*dim.x + dim.x, length(lines[y])));
                            currentAdd++;
                        }
                        for(int c = length(resizedLines[y+added]); c<dim.x; c++){
                            processingLine += " ";
                        }
                    }
                    
                    y++;
                }

                return newCard(resizedLines, dim);
            }
            else {
                return newCard(lines, dim, resize);
            }
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
            int xlen = 0;
            for(int x = 0; x<length(normalized, 2); x++){
                xlen += cards[0][x].dimension.x;
            }
            xlen += length(cards, 2)*2+1;
            String topBorder = new String(new char[xlen + 2]).replace("\0", "#") + "\n";
            s += topBorder;

            String separator = "#" + new String(new char[xlen]).replace("\0", HORIZ_SEPARATOR) + "#";
            String empty = "#" + new String(new char[xlen]).replace("\0", " ") + "#";

            for(int y = 0; y < length(normalized); y ++){
                s += HUD_DisplayCardLine(normalized[y]);
                s += empty+"\n" + separator + "\n"+empty+"\n";
            }
             
            s += topBorder;
            return s;
        }

        final String VERT_SEPARATOR = " | ";
        String HUD_DisplayCardLine(Card[] cards){
            /* MUST be used on a normalized line
            Display a line of cards.
            
            */
            String s = "";
            for(int y = 0; y<length(cards[0].lines); y++){
                String line = "# ";
                for(int x = 0; x<length(cards); x++){
                    line += cards[x].lines[y] + VERT_SEPARATOR;
                }
                s += substring(line, 0, length(line)-length(VERT_SEPARATOR)) + " #\n";
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
        final int HUD_INFOCARD_LENGTH = 60;
        final int HUD_INFOCARD_HEIGHT = 20;

        Card HUD_SO_displayInfosCard(SysObject o, Player p, boolean select, Vector2 pos){
            //Display a card giving the available informations and actions on the given object. Line by Line (to help in building the global HUD format)
            String[] lines = new String[HUD_INFOCARD_HEIGHT];
            Vector2 cardSize;
            if(!select || VECTOR2_distance(p.pos, pos) <= p.ship.SightRange){
                cardSize = newVector2(HUD_INFOCARD_LENGTH, HUD_INFOCARD_HEIGHT);
                lines[0] = HUD_SO_displayCardHead(o, p, select, pos);
                lines[1] = "";
                int i = 2;
                while(i<HUD_INFOCARD_HEIGHT && ((i-2)*HUD_INFOCARD_LENGTH) < length(o.type.description)){
                    lines[i] = substring(o.type.description, (i-2)*HUD_INFOCARD_LENGTH, Math.min(i*HUD_INFOCARD_LENGTH, length(o.type.description)));
                    i++;
                }
                while(i<HUD_INFOCARD_HEIGHT-5){
                    lines[i] = "";
                    i++;
                }
                String oResFE = "";
                String oResH = "";
                String oResO = "";
                lines[i] = "Ressources du corps célèste - ";
                lines[i+1] = "";
                if(o.type.type == ObjectLabel.Void){
                    oResFE = "0";
                    oResH = "0";
                    oResO = "0";
                } else if(o.approached){
                    println(o.baseResources[0]);
                    oResFE = o.availableResources[0] + "/" + o.baseResources[0];
                    oResH = o.availableResources[1] + "/" + o.baseResources[1];
                    oResO = o.availableResources[2] + "/" + o.baseResources[2];
                } else if(o.visited) {
                    int oAvgRes = totalRes(o.availableResources)/3;
                    oResFE = o.availableResources[0] > oAvgRes && oAvgRes != 0 ? "++" : "?";
                    oResH = o.availableResources[1] > oAvgRes && oAvgRes != 0 ? "++" : "?";
                    oResO = o.availableResources[2] > oAvgRes && oAvgRes != 0 ? "++" : "?";
                } else {
                    oResFE = "?";
                    oResH = "?";
                    oResO = "?";
                }
                lines[i+2] = setAsTableLine(new String[]{"Fer :", oResFE, STATUS_UNIT}, STATUS_COLWIDTH);
                lines[i+3] = setAsTableLine(new String[]{"Hydrogène :", oResH, STATUS_UNIT}, STATUS_COLWIDTH);
                lines[i+4] = setAsTableLine(new String[]{"Oxygène :", oResO, STATUS_UNIT}, STATUS_COLWIDTH);
                
            } else {
                lines = new String[]{"Cible hors de portée du télescope."};
                cardSize = newVector2(HUD_INFOCARD_LENGTH, 1);
            }
            
        
            return newCard(lines, cardSize, true);
        }
/*
        Card HUD_SO_displayObsCard(SysObject o, Player p){

        }*/

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
                "Guide"
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

        Card HUD_confirmMenu(String call){
            return HUD_menuCard(ACTIONS_YESNO, call, false);
        }

        Card HUD_confirmMoveMenu(Vector2 dest){
            double distance = VECTOR2_distance(player.pos, dest);
            return HUD_confirmMenu("- Le voyage en " + dest.x + ":" + dest.y + " consommera " + SHIP_hydrogenRequired(distance) + " unités d'hydrogène -");
        }

        Card HUD_confirmApproachMenu(){
            return HUD_confirmMenu("- Entrez en orbite consommera " + APPROACH_COST + " unités d'hydrogène -");
        }

        Card HUD_confirmLandMenu(int cost){
            return HUD_confirmMenu("- " + cost + " unités d'hydrogène sont nécessaires pour atterrir et redécoller -");
        }

        Card HUD_approachMenu(SysObject o){
            String extractH = "";
            if(isIn(unlandable, o.type.type)){
                extractH = "Extraire de l'hydrogène";
            }

            String[] actions = new String[]{
                "Annuler",
                "Atterrir",
                extractH
            };

            return HUD_menuCard(actions, "- Entrez le numéros de l'action à faire -");
        }

        Card HUD_interactObjMenu(SysObject o){
            String[] actions = new String[]{
                player.nearbySOstatus[0] ? "Quitter L'orbite" : "-",
                player.nearbySOstatus[1] ? "Décoler" : "Atterrir",
                (player.nearbySOstatus[0] && isIn(unlandable, o.type.type) && o.availableResources[1]>0) || player.nearbySOstatus[1] && o.availableResources[1]>0 ? "Extraire de l'hydrogène" : "-",
                (player.nearbySOstatus[1] && o.availableResources[0]>0) || o.type.type == ObjectLabel.Star && o.availableResources[0]>0 ? "Extraire du fer" : "-",
                player.nearbySOstatus[1] && o.availableResources[2]>0 ? "Extraire de l'oxygène" : "-"
            };

            println((player.nearbySOstatus[0]) + " && " + isIn(unlandable, o.type.type) + " || " + player.nearbySOstatus[1] + " && " + (o.type.type == ObjectLabel.Icegiant));

            return HUD_menuCard(actions, "- Entrez le numéros de l'action à faire -");
        }

        Card HUD_SOextractMenu(int max){
            String[] lines = new String[]{
                " Indiquez le montant de ressources à extraire.",
                " Notez que plus la quantité de ressource à extraire est élevée",
                " Plus l'extraction sera coûteuse en hydrogène.",
                " Exemple :",
                "Extraire 20 unités de ressources sur une planète qui en contient",
                "20 de base coûtera 22 d'hydrogène, alors que n'en extraire que",
                "10 coûtera 5 d'hydrogène.",
                "",
                "",
                "- Entrez les ressources à extraire (entre 0 et " + max + " )-"
            };

            return newCard(lines, newVector2(HUD_MENU_LENGTH, length(lines)), true);
        }
        
        Card HUD_SOextractConfirmMenu(int extract, int cost){
            return HUD_confirmMenu("- Vous allez extraire " + extract + " unités en consommant " + cost + " d'hydrogène -");
        }

        Card HUD_triedToLandUnlandable(SysObject o){
            String intro = "";
            String risk = "";
            String nickname = "";

            if(o.type.type == ObjectLabel.Star){
                intro = "approchez lentement de la surface de l'étoile, la température extérieure s'envole";
                risk = "fondre sur lui même !";
                nickname = "du monstre de flammes.";
            } else {
                intro = "enfoncez lentement dans l'épaisse athmosphère de la géante gazeuse, la pression ne cesse d'augmenter";
                risk = "se faire compacter, vous dedans !";
                nickname = "du titan de vents.";
            }
            String[] lines = new String[]{
                "Alors que vous vous  " + intro + ", les appareils sifflent et les alarmes s'excitent.",
                "Si vous descendez plus bas, c'est certains, tout le vaisseau va " + risk,
                "Dans la panique, vous vous extirpez de justesse à la gravité " + nickname + ". Mais non sans dégâts ..",
                "",
                "",
                "",
                "",
                ""
            };
            return newCard(lines, newVector2(HUD_MENU_LENGTH, 8), true, false);
        }

        Card HUD_inspectMenu(){
            String[] actions = new String[]{
                "Annuler"
            };
            return HUD_menuCard(actions, "- Entrez les coordonnées d'observation (x:y) -");
        }

        Card HUD_insufficientHydrogen(int cost){
            String[] lines = new String[]{
                "Hydrogène insuffisant.",
                "L'action nécessite " + cost + " unités d'hydrogène.",
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
            String sign = "";

            String[] res = new String[]{
                player.ownedResources[0]+"",
                player.ownedResources[1]+"",
                player.ownedResources[2]+""
            };

            //#region Preview
            for(int i = 0; i<length(res); i++){
                totalPrev += resPreview[i];
                totalRes += player.ownedResources[i];
                sign = resPreview[i] > 0 ? "+" : "";
                if(resPreview[i] != 0){
                    res[i] = (player.ownedResources[i] + resPreview[i]) + "(" + sign + resPreview[i] + ")";
                }
            }

            if(totalPrev != 0){
                sign = totalPrev > 0 ? "+" : ""; 
                total = (totalRes+totalPrev) + "(" + sign + totalPrev + ")";
            } else {
                total = totalRes + "";
            }

            if(hpPreview != 0){
                sign = hpPreview > 0 ? "+" : "";
                hp = (((player.ship.HPs+hpPreview)*100)/player.ship.MaxHPs) + "(" + sign + hpPreview*100/player.ship.MaxHPs + ")";
            } else {
                hp = player.ship.HPs*100/player.ship.MaxHPs + "";
            }

            if(storePreview != 0){
                sign = storePreview > 0 ? "+" : "";
                store = player.ship.Storage+storePreview + "(" + sign + storePreview + ")";
            } else {
                store = player.ship.Storage+"";
            }
            //#endregion

            String[] lines = new String[]{
                "Ressources -",
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

        Card HUD_timePreview(int time){
            String line = "Estimation du temps requis : " + hoursToString(time);
            return newCard(new String[]{line}, newVector2(length(line), 1));
        }
    
        String[] HUD_numberList(String [] list){
            for(int i = 0; i<length(list); i++){
                list[i] = i + ". " + list[i];
            }
            return list;
        }

        //#endregion
    //#endregion

    //#region GMP   | RUNTIME GAMEPLAY
    void GMP_InflictDamage(int damage){
        if(!SHIP_InflictDamage(player.ship, damage)){
            GMP_endGame(2);
        }
    }
    void GMP_insufficientFE(int amount){
        win_botLeft = HUD_insufficientFe(amount);
        GMP_displayHUD();
        waitSeconds(TEMPWINDOW);
    }

    void GMP_system(){
        win_timePreview = newCard();
        win_topLeft = SYS_playerVisionToCard(sys_current, player);
        win_topRight = HUD_SO_displayInfosCard(sys_current[player.pos.y][player.pos.x], player);
        win_botLeft = HUD_SYS_mainMenu(sys_current[player.pos.y][player.pos.x]);
        win_botRight = HUD_Status();

        GMP_displayHUD();

        input = readString();
        if(equals(input,"1")){
            GMP_move();
        } else if(equals(input, "2")){
            GMP_inspect();
        } else if(equals(input, "3")){
            GMP_approach();
        } else if(equals(input, "4")){
            GMP_manageShip();
        } else if(equals(input, "5")){
            GMP_displayGuide();
        }
        GMP_system();
    }

    //#region CMD parameters Names
    //Common
    final String CMDP_IRON = "fe";
    final String CMDP_HYDROGEN = "h";
    final String CMDP_OXYGEN = "o";
    final String CMDP_APPROACHED = "app";
    final String CMDP_NAME = "name";

    //SO specific
    final String CMDP_APPROACHRISKS = "arisk";
    final String CMDP_LANDINGRISKS = "lrisk";
    final String CMDP_MASS = "mass";
    final String CMDP_TYPE = "type";
    final String CMDP_VISITED = "visit";
    
    //PLAYER specific
    final String CMDP_POSITION = "pos";
    final String CMDP_HPS = "hps";
    final String CMDP_MAXHPS = "maxhps";
    final String CMDP_SIGHTRANGE = "sight";
    final String CMDP_STORAGE = "store";
    final String CMDP_LANDED = "land";

    //GEN
    final String CMDG_VOID = "void";
    final String CMDG_STAR = "star";
    final String CMDG_TELLURIC = "tell";
    final String CMDG_GASGIANT = "gas";
    final String CMDG_ICEPLANET = "ice";
    final String CMDG_ASTEROID = "astd";
    final String CMDG_COMET = "comet";
    //#endregion

    void GMP_adminConsole(String[] params){
        /*
        The implementation of this console is very rudimentary.
        It would be much smarter to use oop, or at least delegate, but im not sur we're allowed to do so.
         */
        if(equals(params[0], "help")){
            File f = newFile("../ressources/man/manADMIN.txt");
            while(ready(f)){
                logCache+=readLine(f)+"\n";
            }
        } else if(equals(params[0], "set") || equals(params[0], "log")){
            int cmd_start = INPUT_isValidCoord(params[1]) ? 1 : 0;
            String cmd_stringValue = "";
            if(length(params)>2+cmd_start){
                cmd_stringValue = params[2+cmd_start];
            }
            Vector2 cmd_vectorValue = VECTOR2_NULL;
            int cmd_value = 0;
            if(isNumeric(cmd_stringValue)){
                cmd_value = Integer.parseInt(cmd_stringValue);
            } else if(INPUT_isValidCoord(cmd_stringValue, sys_current)){
                cmd_vectorValue = INPUT_inputToVector2(cmd_stringValue);
            }

            String cmd_param = params[1+cmd_start];
            if(cmd_start == 1){
                Vector2 cmd_coord = INPUT_inputToVector2(params[1]);
                SysObject cmd_target = sys_current[cmd_coord.y][cmd_coord.x];
                if(equals(params[0], "log")){
                    if(equals(cmd_param, CMDP_IRON)){
                        logCache = ""+cmd_target.availableResources[0];
                    } else if(equals(cmd_param, CMDP_HYDROGEN)){
                        logCache = ""+cmd_target.availableResources[1];
                    } else if(equals(cmd_param, CMDP_OXYGEN)){
                        logCache = ""+cmd_target.availableResources[2];
                    } else if(equals(cmd_param, CMDP_APPROACHRISKS)){
                        logCache = ""+cmd_target.approachRisks;
                    } else if(equals(cmd_param, CMDP_LANDINGRISKS)){
                        logCache = ""+cmd_target.landingRisks;
                    } else if(equals(cmd_param, CMDP_MASS)){
                        logCache = ""+cmd_target.mass;
                    } else if(equals(cmd_param, CMDP_NAME)){
                        logCache = ""+cmd_target.name;
                    } else if(equals(cmd_param, CMDP_TYPE)){
                        logCache = ""+cmd_target.type.type.displayType;
                    } else if(equals(cmd_param, CMDP_VISITED)){
                        logCache = ""+cmd_target.visited;
                    } else if(equals(cmd_param, CMDP_APPROACHED)){
                        logCache = ""+cmd_target.approached;
                    } else {
                        logCache = "Unknown parameter";
                    }
                } else if(equals(params[0], "set")){
                    logCache = " of object in " + cmd_coord.x + ":" + cmd_coord.y;
                    if(isNumeric(cmd_stringValue)){
                        if(equals(cmd_param, CMDP_IRON)){
                            cmd_target.availableResources[0] = cmd_value;
                            logCache = "Iron" + logCache;
                        } else if(equals(cmd_param, CMDP_HYDROGEN)){
                            cmd_target.availableResources[1] = cmd_value;
                            logCache = "Hydrogen" + logCache;
                        } else if(equals(cmd_param, CMDP_OXYGEN)){
                            cmd_target.availableResources[2] = cmd_value;
                            logCache = "Oxygen" + logCache;
                        } else if(equals(cmd_param, CMDP_APPROACHRISKS)){
                            cmd_target.approachRisks = cmd_value;
                            logCache = "Approach Risks" + logCache;
                        } else if(equals(cmd_param, CMDP_LANDINGRISKS)){
                            cmd_target.landingRisks = cmd_value;
                            logCache = "Landing Risks" + logCache;
                        } else if(equals(cmd_param, CMDP_MASS)){
                            cmd_target.mass = cmd_value;
                            logCache = "Mass" + logCache;
                        } else if(equals(cmd_param, CMDP_TYPE)){
                            logCache = "(-) object type cannot be set.";
                        } else if(equals(cmd_param, CMDP_VISITED)){
                            cmd_target.visited = cmd_value != 0;
                            logCache = "Visited" + logCache;
                        } else if(equals(cmd_param, CMDP_APPROACHED)){
                            cmd_target.approached = cmd_value != 0;
                            logCache = "Approached" + logCache;
                        } else {
                            logCache = "";
                        }
                    } else {
                        if(equals(cmd_param, CMDP_NAME)){
                            cmd_target.name = cmd_stringValue;
                            logCache = "Name" + logCache;
                        } else {
                            logCache = "";
                        }
                    } 

                    GMP_adminConsoleSetCache();
                }
            } else {
                if(equals(params[0], "log")){
                    if(equals(cmd_param, CMDP_IRON)){
                        logCache = ""+player.ownedResources[0];
                    } else if(equals(cmd_param, CMDP_HYDROGEN)){
                        logCache = ""+player.ownedResources[1];
                    } else if(equals(cmd_param, CMDP_OXYGEN)){
                        logCache = ""+player.ownedResources[2];
                    } else if(equals(cmd_param, CMDP_POSITION)){
                        logCache = ""+player.pos.x+":"+player.pos.y;
                    } else if(equals(cmd_param, CMDP_HPS)){
                        logCache = ""+player.ship.HPs;
                    } else if(equals(cmd_param, CMDP_MAXHPS)){
                        logCache = ""+player.ship.MaxHPs;
                    } else if(equals(cmd_param, CMDP_NAME)){
                        logCache = ""+player.name;
                    } else if(equals(cmd_param, CMDP_SIGHTRANGE)){
                        logCache = ""+player.ship.SightRange;
                    } else if(equals(cmd_param, CMDP_STORAGE)){
                        logCache = ""+player.ship.Storage;
                    } else if(equals(cmd_param, CMDP_APPROACHED)){
                        logCache = ""+player.nearbySOstatus[0];
                    } else if(equals(cmd_param, CMDP_LANDED)){
                        logCache = ""+player.nearbySOstatus[1];
                    } else if(equals(cmd_param, "time")){
                        logCache = ""+game_time;
                    } else {
                        logCache = "Unknown parameter";
                    }
                } else if(equals(params[0], "set")){
                    logCache = " of player " + player.name;
                    if(isNumeric(cmd_stringValue)){
                        if(equals(cmd_param, CMDP_IRON)){
                            player.ownedResources[0] = cmd_value;
                            logCache = "Iron" + logCache;
                        } else if(equals(cmd_param, CMDP_HYDROGEN)){
                            logCache = "Hydrogen" + logCache;
                            PLAYER_consumeHydrogen(player,player.ownedResources[1]-cmd_value);
                        } else if(equals(cmd_param, CMDP_OXYGEN)){
                            player.ownedResources[2] = cmd_value;
                            logCache = "Oxygen" + logCache;
                        } else if(equals(cmd_param, CMDP_HPS)){
                            logCache = "HPs" + logCache;
                            if(!SHIP_InflictDamage(player.ship, player.ship.HPs-cmd_value)){
                                GMP_endGame(2);
                            }
                        } else if(equals(cmd_param, CMDP_MAXHPS)){
                            player.ship.MaxHPs = cmd_value;
                            logCache = "MaxHPs" + logCache;
                        } else if(equals(cmd_param, CMDP_SIGHTRANGE)){
                            player.ship.SightRange = cmd_value;
                            logCache = "Sight Range" + logCache;
                        } else if(equals(cmd_param, CMDP_STORAGE)){
                            player.ship.Storage = cmd_value;
                            logCache = "Storage" + logCache;
                        } else if(equals(cmd_param, CMDP_APPROACHED)){
                            player.nearbySOstatus[0] = cmd_value != 0;
                            logCache = "Approach Status" + logCache;
                        } else if(equals(cmd_param, CMDP_LANDED)){
                            player.nearbySOstatus[1] = cmd_value != 0;
                            logCache = "Land Status" + logCache;
                        } else if(equals(cmd_param, "xpos")){
                            player.pos.x += cmd_value;
                            logCache = "Xpos" + logCache;
                        } else if(equals(cmd_param, "time")){
                            logCache = "Time" + logCache;
                            GMP_spendTime(cmd_value-game_time);
                        } else {
                            logCache = "";
                        }
                    } else if (INPUT_isValidCoord(cmd_stringValue)){
                        if(equals(cmd_param, CMDP_POSITION)){
                            player.pos = cmd_vectorValue;
                            logCache = "Position" + logCache;
                        } else {
                            logCache = "";
                        }
                    } else {
                        if(equals(cmd_param, CMDP_NAME)){
                            player.name = cmd_stringValue;
                            logCache = "Name" + logCache;
                        } else {
                            logCache = "";
                        }
                    }
                    win_botRight = HUD_Status();
                    win_topLeft = SYS_playerVisionToCard(sys_current, player);
                    win_topRight = HUD_SO_displayInfosCard(sys_current[player.pos.y][player.pos.x], player);
                    GMP_adminConsoleSetCache();
                }
            }
        } else if(equals(params[0], "gen")){
            GMP_adminConsoleGEN(params);

            win_topLeft = SYS_playerVisionToCard(sys_current, player);
            win_topRight = HUD_SO_displayInfosCard(sys_current[player.pos.y][player.pos.x], player);
        }
    }

    void GMP_adminConsoleGEN(String[] params){
        Vector2 cmd_coord = INPUT_inputToVector2(params[1]);
        ObjectLabel cmdg_label = ObjectLabel.Void;
        if(length(params)>2 && !equals(params[2], CMDG_VOID)){
            if(equals(params[2], CMDG_STAR)){
                cmdg_label = ObjectLabel.Star;
            } else if(equals(params[2], CMDG_TELLURIC)){
                cmdg_label = ObjectLabel.Telluric;
            } else if(equals(params[2], CMDG_GASGIANT)){
                cmdg_label = ObjectLabel.Gasgiant;
            } else if(equals(params[2], CMDG_ICEPLANET)){
                cmdg_label = ObjectLabel.Icegiant;
            }
        }
        sys_current[cmd_coord.y][cmd_coord.x] = newSysObject(cmdg_label);
    }

    void GMP_adminConsoleSetCache(){
        if(equals(logCache, "")){
            logCache = "Error in the input";
        } else{
            logCache = "Successfully set " + logCache;
        }
    }

    final double TEMPWINDOW = 1.8;

    void GMP_inspect(){
        win_botLeft = HUD_inspectMenu();

        GMP_displayHUD();;

        input = readString();

        while(INPUT_isValidCoord(input, sys_current)){
            Vector2 inputCoord = INPUT_inputToVector2(input);
            
            win_botRight = HUD_SO_displayInfosCard(sys_current[inputCoord.y][inputCoord.x], player, true, inputCoord);

            GMP_displayHUD();

            input = readString();
        }
        if(isCommand(input)){
            GMP_inspect();
        } else {
            GMP_system();
        }
    }

    void GMP_move(){
        win_botLeft = HUD_moveMenu();

        GMP_displayHUD();

        input = readString();

        if(INPUT_isValidCoord(input, sys_current)){
            Vector2 inputCoord = INPUT_inputToVector2(input);
            double distance = VECTOR2_distance(player.pos, inputCoord);
            int cost = SHIP_hydrogenRequired(distance);
            win_timePreview = HUD_timePreview((int)Math.ceil(distance/player.ship.Speed));
            win_botLeft = HUD_confirmMoveMenu(inputCoord);
            win_botRight = HUD_Status(new int[]{0, -cost, 0});

            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_move(player, inputCoord)){
                    win_botLeft = HUD_insufficientHydrogen(cost);
                    GMP_displayHUD();
                    waitSeconds(TEMPWINDOW);
                }
            }
            GMP_system();
        } else if(isCommand(input)){
            GMP_move();
        } else {
            GMP_system();
        }
    }

    final int APPROACH_COST = 2;

    void GMP_approach(){
        SysObject o = sys_current[player.pos.y][player.pos.x];
        if(o.type.type != ObjectLabel.Void){
            win_botLeft = HUD_confirmApproachMenu();
            win_botRight = HUD_Status(new int[]{0, -APPROACH_COST, 0});

            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(PLAYER_approach(player)){
                    GMP_approached(o);
                } else {
                    win_botLeft = HUD_insufficientHydrogen(APPROACH_COST);
                    GMP_displayHUD();
                    waitSeconds(TEMPWINDOW);
                }          
            }
        }
        GMP_system();
    }

    void GMP_approached(SysObject o){
        win_topRight = HUD_SO_displayInfosCard(o, player);
        win_botRight = HUD_Status();
        win_botLeft = HUD_interactObjMenu(o);
        GMP_displayHUD();

        input = readString();

        if(equals(input, "1")){
            GMP_land(o);
        } else if(equals(input, "2") && isIn(unlandable, o.type.type) && o.availableResources[1]>0) {
            GMP_extract(o, 1);
        } else if(equals(input, "3") && isIn(unlandable, o.type.type) && o.availableResources[0]>0) {
            GMP_extract(o, 0);
        } else if(!equals(input, "0") || isCommand(input)){
            GMP_approached(o);
        }
    }

    final int UNLANDABLE_DAMAGE = 20;

    void GMP_land(SysObject o){
        int cost = o.landingCost;
        win_botLeft = HUD_confirmLandMenu(cost);
        win_botRight = HUD_Status(new int[]{0, -cost, 0});

        GMP_displayHUD();

        input = readString();

        if(confirm(input)){
            if(!PLAYER_land(player, o)){
                win_botLeft = HUD_insufficientHydrogen(cost);
                GMP_displayHUD();
                waitSeconds(TEMPWINDOW);
                GMP_approached(o);
            } else if(isIn(unlandable, o.type.type)){
                player.nearbySOstatus[1] = false;
                player.nearbySOstatus[0] = true;
                win_botLeft = HUD_triedToLandUnlandable(o);
                win_botRight = HUD_Status(emptyResources(), -UNLANDABLE_DAMAGE, 0);
                GMP_InflictDamage(UNLANDABLE_DAMAGE);
                GMP_displayHUD();
                readString();
                GMP_approached(o);
            } else {
                GMP_landed(o);
            }
        } else {
            GMP_approached(o);
        }
    }

    void GMP_landed(SysObject o){
        win_botRight = HUD_Status();
        win_botLeft = HUD_interactObjMenu(o);
        GMP_displayHUD();

        input = readString();

        if(equals(input, "1")){
            player.nearbySOstatus[0] = true;
            player.nearbySOstatus[1] = false;
            GMP_approached(o);
        } else if(equals(input, "2") && o.availableResources[1]>0) {
            GMP_extract(o, 1);
        } else if(equals(input, "3") && o.availableResources[0]>0) {
            GMP_extract(o, 0);
        } else if(equals(input, "4") && o.availableResources[2]>0) {
            GMP_extract(o, 2);
        } else {
            GMP_landed(o);
        }
    }

    int SO_HcostForResource(SysObject o, int resIdx, int res){
        //The more the player extracts resources, the more expansive it gets. + an object with more base resources allows for easier extraction.
        return (int)(Math.pow(((o.baseResources[resIdx]-o.availableResources[resIdx]+res)*1.0)/o.baseResources[resIdx]*1.0,2)*Math.pow(o.baseResources[resIdx], 0.5)*5);
    }

    void GMP_extract(SysObject o, int resIdx){
        win_botLeft = HUD_SOextractMenu(o.baseResources[resIdx]);

        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int extractAmount = Integer.parseInt(input);
            extractAmount = Math.min(Math.min(extractAmount, player.ship.Storage - totalRes(player.ownedResources)), o.availableResources[resIdx]);
            int extractCost = SO_HcostForResource(o, resIdx, extractAmount);
            
            int[] previewRes = {0, -extractCost, 0};
            previewRes[resIdx] += extractAmount;
            win_timePreview = HUD_timePreview(intDivToCeil(extractCost, 4)+1);
            int damage = 0;
            
            if(o.type.type == ObjectLabel.Star){
                damage = -extractCost*2 - 1;
            }
            win_botRight = HUD_Status(previewRes, damage, 0);
            win_botLeft = HUD_SOextractConfirmMenu(extractAmount, extractCost);

            GMP_displayHUD();

            input = readString();
            if(confirm(input)){
                if(!PLAYER_extract(player, o, resIdx, extractAmount)){
                    win_botLeft = HUD_insufficientHydrogen(extractCost);
                    GMP_displayHUD();
                    waitSeconds(TEMPWINDOW);
                    GMP_extract(o, resIdx);
                }
                win_topRight = HUD_SO_displayInfosCard(o, player);
            }
        } else if(isCommand(input)){
            GMP_extract(o, resIdx);
        }

        if(player.nearbySOstatus[0]){
            GMP_approached(o);
        } else if(player.nearbySOstatus[1]){
            GMP_landed(o);
        }
        
    }

    //#region Ship Management
    void GMP_manageShip(){
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
        } else if(isCommand(input)){
            GMP_manageShip();
        }
        GMP_system();
    }

    void GMP_shipRepair(){
        win_botLeft = HUD_shipRepairMenu();
        GMP_displayHUD();

        input = readString();
        int feAmount = INPUT_repairInputToFe(input);
        int repairAmount = SHIP_hpsForIron(feAmount);
        int repairPerc = ((repairAmount + player.ship.HPs)*100)/player.ship.MaxHPs;
        if(feAmount>0){
            win_timePreview = HUD_timePreview(intDivToCeil(repairAmount, 10));
            win_botLeft = HUD_shipRepairConfirmMenu(feAmount, repairPerc);
            win_botRight = HUD_Status(new int[]{-feAmount, 0, 0}, repairAmount, 0);

            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_repair(player, feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        } else if(isCommand(input)){
            GMP_shipRepair();
        }
    }

    void GMP_shipReinforce(){
        win_botLeft = HUD_shipReinforceMenu();
        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int feAmount = Integer.parseInt(input);
            win_timePreview = HUD_timePreview(intDivToCeil(SHIP_hpsForIron(feAmount/4), 2));
            win_botLeft = HUD_shipReinforceConfirmMenu();
            win_botRight = HUD_Status(new int[]{-feAmount, 0, 0});
            GMP_displayHUD();

            input = readString();

            if(confirm(input)){    
                if(!PLAYER_reinforce(player, feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        } else if(isCommand(input)){
            GMP_shipReinforce();
        }
    }

    void GMP_shipTelescopeUpgrade(){
        win_botLeft = HUD_shipTelescopeUpgradeMenu();
        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int feAmount = Integer.parseInt(input)*SHIP_SIGHTTOIRON;
            win_timePreview = HUD_timePreview(Integer.parseInt(input));
            win_botLeft = HUD_shipTelescopeUpgradeConfirmMenu();
            win_botRight = HUD_Status(new int[]{-feAmount, 0, 0});
            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_upgradeTelescope(player, feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        } else if(isCommand(input)){
            GMP_shipTelescopeUpgrade();
        }
    }

    void GMP_shipStorageUpgrade(){
        win_botLeft = HUD_shipStorageUpgradeMenu();
        GMP_displayHUD();

        input = readString();
        if(isNumeric(input)){
            int feAmount = Integer.parseInt(input)*SHIP_STORAGETOIRON;
            win_timePreview = HUD_timePreview(intDivToCeil(Integer.parseInt(input), 5));
            win_botLeft = HUD_shipStorageUpgradeConfirmMenu();
            win_botRight = HUD_Status(new int[]{-feAmount, 0, 0}, 0, Integer.parseInt(input));
            GMP_displayHUD();

            input = readString();

            if(confirm(input)){
                if(!PLAYER_upgradeStorage(player,feAmount)){
                    GMP_insufficientFE(feAmount);
                }
            }
        } else if(isCommand(input)){
            GMP_shipStorageUpgrade();
        }
    }
    //#endregion

    void GMP_displayHUD(){
        clearScreen();
        if(isAdmin){
            GMP_adminConsole(input.split(" "));
        }
        String line = "Temps écoulé : " + hoursToString(game_time);
        win_gameTime = newCard(new String[]{line}, newVector2(length(line), 1));
        println(HUD_DisplayCardCluster(new Card[][]{{win_gameTime, win_timePreview},{win_topLeft, win_topRight},{win_botLeft, win_botRight}}));
        println(logCache);
        logCache = "";
    }

    void GMP_newGame(){
        println("Entrez votre nom de joueur : ");
        String newName = readString();
        Player newPlayer = initPlayer(newName, newVector2(20, 20));
        newPlayer.ship.HPs = 50;
        File f = newFile("../ressources/intro.txt");
        while(ready(f)){
            println(readLine(f));
        }
        readString();
        GMP_displayGuide();
        
        GMP_initGame(newPlayer, equals(newName, "admin"), SYS_generateSystem(), 0);
    }

    void GMP_displayGuide(){
        File f = newFile("../ressources/howToPlay.txt");
        while(ready(f)){
            println(readLine(f));
        }
        readString();
    }

    void GMP_initGame(Player p, boolean adminSession, SysObject[][] system, int time){
        player = p;
        isAdmin = adminSession;
        sys_current = system;
        sys_current[p.pos.y][p.pos.x].visited = true;
        game_time = time;
        GMP_system();
    }

    void GMP_spendTime(int time){
        int realTime = Math.min(time, player.ownedResources[2]*12-game_lastOxTime);
        game_time += realTime;
        game_lastOxTime += realTime;
        if(game_lastOxTime > 12){
            if(!PLAYER_consumeOxygen(player, game_lastOxTime/12)){
                GMP_endGame(0);
            };
            game_lastOxTime %= 12;
        }
    }

    void GMP_mainMenu(){
        File f = newFile("../ressources/bootMenu.txt");
        while(ready(f)){
            println(readLine(f));
        }
        input = readString();
        print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        if(equals(input, "2")){
        } else if(equals(input, "3")){
            GMP_displayScore();
        } else {
            GMP_newGame();
        }
    }

    void GMP_endGame(int type){
        String fileSource = "";
        if(type == 0){
            fileSource = "../ressources/ends/oxygenEnd.txt";
        } else if(type == 1){
            if(isIn(new ObjectLabel[]{ObjectLabel.Icegiant, ObjectLabel.Telluric}, sys_current[player.pos.x][player.pos.y].type.type)){
                fileSource = "../ressources/ends/stuckInSpaceLandedEnd.txt";
            } else {
                fileSource = "../ressources/ends/stuckInSpaceEnd.txt";
            }
        } else if(type == 2){
            fileSource = "../ressources/ends/shipDestroyedEnd.txt";
        }

        File f = newFile(fileSource);

        while(ready(f)){
            println(readLine(f));
        }
        String reCenter = new String(new char[60]).replace("\0", " ");
        println(breakLine + "\n" + reCenter + " - Vous avez survécu " + hoursToString(game_time));
        println(reCenter + " Voulez-vous enregistrer votre score ?");
        println(reCenter + "0. Oui         1. Non");
        print(breakLine+"\n"+reCenter+"#>");
        if(equals(readString(),"1")){
            GMP_mainMenu(); 
        } else {
            addScore(new String[]{player.name, game_time+""});
            GMP_displayScore();
        }
    }

    void addScore(String[] score){
        CSVFile csv_scores = loadCSV(scoreRef);
        String[][] s_scores = new String[rowCount(csv_scores) + 1][2];
        int y = 0;
        int added = 0;
        while(y < length(s_scores)){
            if(added == 1 || (y<rowCount(csv_scores) && Integer.parseInt(getCell(csv_scores, y, 1))>Integer.parseInt(score[1]))){
                s_scores[y] = new String[]{getCell(csv_scores, y-added, 0), getCell(csv_scores, y-added, 1)};
            } else {
                s_scores[y] = score;
                added ++;
            }
            y ++;
        }

        saveCSV(s_scores, scoreRef);
    }

    void GMP_displayScore(){
        CSVFile csv_scores = loadCSV(scoreRef);
        String margin = new String(new char[15]).replace("\0", " ");

        println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        println(margin + "Joueur" + new String(new char[24]).replace("\0", " ") + "|  A survécu");
        println(margin + new String(new char[61]).replace("\0", "_"));
        
        for(int y = 0; y<rowCount(csv_scores); y++){
            String p_name = getCell(csv_scores, y, 0);
            String p_score = hoursToString(Integer.parseInt(getCell(csv_scores, y, 1)));

            println(margin + "(" + (y+1) + ") - " + p_name + new String(new char[30-length(p_name)-5-(y+11)/10]).replace("\0", " ") + "| " + p_score);
        }

        readString();
        GMP_mainMenu();
    }

    //#endregion

    //#region Game global variables
    String scoreRef = "../ressources/scores.csv";
    String breakLine = new String(new char[200]).replace("\0", "_");
    boolean isAdmin;

    int game_time;
    int game_lastOxTime;

    String input = "";

    Player player;
    Player player2;
    Player player3;

    Card win_gameTime;
    Card win_timePreview;
    Card win_topLeft;
    Card win_topRight;
    Card win_botLeft;
    Card win_botRight;

    String logCache = "";

    SysObject[][] sys_current;
    //#endregion

    void algorithm() {
        GMP_mainMenu();
    }

    //#region TESTS
    void test_hoursToString(){
        assertTrue(equals(hoursToString(25), "1 jours, 1 heure."));
        assertTrue(equals(hoursToString(365), "1 années."));
        assertTrue(equals(hoursToString(390), "1 années, 1 jours, 1 heure."));
    }

    void test_isNumeric(){
        assertTrue(isNumeric('1'));
        assertTrue(isNumeric("1"));
        assertFalse(isNumeric('a'));
        assertFalse(isNumeric("123b5"));
    }

    void test_isCommand(){
        assertTrue(isCommand(COMMAND_PREFIXES[(int)(random()*length(COMMAND_PREFIXES))]));
        assertTrue(isCommand(COMMAND_PREFIXES[(int)(random()*length(COMMAND_PREFIXES))]));
        assertTrue(isCommand(COMMAND_PREFIXES[(int)(random()*length(COMMAND_PREFIXES))]));
        assertFalse(isCommand("give"));
    }

    void test_combineArrays(){
        String[][] testArr = new String[][]{{"a","b","c"},{"d","e","f"}};
        String[] testArrComb = combineArrays(testArr);
        for(int i = 0; i<6; i ++){
            assertTrue(equals(testArr[i/3][i%3], testArrComb[i]));
        }
    }

    void test_enlargeArray(){
        String[] testArr = new String[]{"a","b","c"};
        String[] testArrEn = enlargeArray(testArr, 5);
        assertEquals(length(testArrEn), length(testArr) + 5);
        assertTrue(equals(testArrEn[1],testArr[1])); 
    }

    void test_INPUT_isValidCoord(){
        assertTrue(INPUT_isValidCoord("5:1"));
        assertTrue(INPUT_isValidCoord("5;1"));
        assertFalse(INPUT_isValidCoord("05:1"));
        assertFalse(INPUT_isValidCoord("5-1"));
        assertFalse(INPUT_isValidCoord(":1"));
        assertFalse(INPUT_isValidCoord("1:"));
        assertFalse(INPUT_isValidCoord(":"));
        assertFalse(INPUT_isValidCoord("55"));
        assertTrue(INPUT_isValidCoord("100;100"));
        assertFalse(INPUT_isValidCoord("5:-1"));
    }

    void test_INPUT_inputToVector2(){
        Vector2 testVec = newVector2(5, 20);
        assertEquals(testVec.x, INPUT_inputToVector2("5:20").x);
        assertEquals(testVec.y, INPUT_inputToVector2("5:20").y);
    }

    //#endregion
}
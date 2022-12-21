enum ObjectLabel{
    Telluric("Planète Tellurique",'o'),     //Only contains Iron
    Gasgiant("Géante Gazeuse", 'O'),        //Only contains Hydrogen
    Icegiant("Planète de glace", 'o'),      //Contains Hydrogen and Oxygen
    Asteroid("Astéroïde", '-'),             //behaves like telluric but moves through space, generated randomly during the game
    Comet("Comète", '-'),                   //behaves like icegiant but moves through space, generated randomly during the game
    Nebula("Nébuleuse", 's'),               //Contains everything in small quantities, very easy to extract
    Star("Etoile", '*'),                    //Contains Iron and Hydrogen, much more dangerous to extract
    Void("Vide intersidéral", ' '),
    Undetermined("?",'?');

    final String displayType;

    final char sprite;

    ObjectLabel(String displayType, char sprite){
        this.displayType = displayType;
        this.sprite = sprite;
    }
}

class ObjectType {
    ObjectLabel type;

    //String subType;
    String description;
    //SysObject[] moons;
    //..

    
    //Function to convert a db into a ObjectType[]
}
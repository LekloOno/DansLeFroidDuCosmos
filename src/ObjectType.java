enum ObjectLabel{
    /*
    Object Label represents the type of a celestial/system object.
    It is mostly a functional tool, to process interactions, but also into the display of objects.
     */
    Telluric("Planète Tellurique",'o'),     //Only contains Iron
    Gasgiant("Géante Gazeuse", 'O'),        //Only contains Hydrogen
    Icegiant("Planète de glace", 'o'),      //Contains Hydrogen and Oxygen
    Asteroid("Astéroïde", '-'),             //behaves like telluric but moves through space, generated randomly during the game
    Comet("Comète", '-'),                   //behaves like icegiant but moves through space, generated randomly during the game
    Nebula("Nébuleuse", 's'),               //Contains everything in small quantities, very easy to extract
    Star("Etoile", '*'),                    //Contains Iron and Hydrogen, much more dangerous to extract
    Void("Vide intersidéral", ' '),
    Undetermined("?",'?');

    /* Technically some oop here, it was just much much much more convenient
    Nonetheless, it could be recreated with -
        - a "String OL_displayType(ObjectLabel)" function :
            which just compares the label to the differents ones, and returns the appropriated displayType string.
        - a "char OL_sprite(ObjectLabel)" function :
            which do the same for sprites.

    Instead of calling objectlabel.displayType, we would then call OL_displayType(objectlabel)
    Same operation for sprites.
    */
    final String displayType;

    final char sprite;

    ObjectLabel(String displayType, char sprite){
        this.displayType = displayType;
        this.sprite = sprite;
    }
}

class ObjectType {
    ObjectLabel type;

    String description;
    //SysObject[] moons;
    //..
}
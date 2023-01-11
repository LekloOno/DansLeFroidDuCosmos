class SysObject {
    /*
    Represents a celestial object in a system. 
     */
    boolean visited;                //true if the player has already visited the position of the object.
    boolean approached;             //true if the player has already approached or landed on the object.

    String name;                    //Non mandatory, the type displayType will be used instead if it is null. Will be used to create specific objects.
    ObjectType type;                //See ObjectType.java
    double mass;                    //Will influence the landing and approach risks, as well as the starting available resources.
    int landingCost;                //Equivalent to the amount of Hydrogen required.
    double landingRisks;            //Not an explicit value.
    double approachRisks;           //Not an explicit value.
    int[] availableResources;       //See region RES | Resources in Main.java       The contained resources of the object.
    int[] baseResources;
}
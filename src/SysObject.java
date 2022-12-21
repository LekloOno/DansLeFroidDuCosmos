class SysObject {
    //Vector2 pos;
    boolean visited;
    boolean approached;

    String name;
    ObjectType type;
    double mass;
    int landingCost;
    double landingRisks;           //Not an explicit value
    double approachRisks;          //Not an explicit value
    int[] availableResources;
    /*
    Such as
    [0] = FE
    [1] = Hydrogen
    [2] = Oxygen
    */
}


/*
Position,visited,approched,typeID,mass,availableResources

typeID refers to a type in ObjectTypes.csv

LandingCost, risks, and approach risks aren't stored, and will be computed again when loading


*/
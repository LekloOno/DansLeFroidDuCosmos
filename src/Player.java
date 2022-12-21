class Player {
    String name;
    Vector2 pos;
    Ship ship;
    int[] ownedResources;
    /*
    Such as
    [0] = FE
    [1] = Hydrogen
    [2] = Oxygen
    */
    boolean[] nearbySOstatus = new boolean[5];
    /*
    Such as
    [0] = approached ?
    [1] = landed ?                requires [1]
    [2] = H can be collected ?    requires [0]
    [3] = Fe can be collected ?   requires [1]
    [4] = O can be collected ?    requires [1]
    */
}
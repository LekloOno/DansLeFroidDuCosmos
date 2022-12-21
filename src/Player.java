class Player {
    String name;                                //Name of the player.
    Vector2 pos;                                //Position of the player in the system.
    Ship ship;                                  //See ship.java                                 Reference to his ship. 
    int[] ownedResources;                       //See region RES | RESOURCES in Main.java       Currently owned resources. 
    boolean[] nearbySOstatus = new boolean[5];  //See NBS | NEARBY STATUS in Main.java          Spacial status in the current system tile.
}
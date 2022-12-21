class Ship {
    int HPs;            //Current HPs of the Ship. If it gets to 0, the game is over.
    int MaxHPs;         //MaxHPs of the Ship. The ship can't be repair above this amount of HPs.
    double SightRange;  //In system unit. Represents how far the player can identify objects in the system.
    double Efficiency;  //Non explicit value - the higher it is, the lower the cost to move.
    //To be implemented later - int ArmorLevel; //A way to completely nullify small enough damage. The value represents its threshold.
}
class SO_GenSettings {
    /*
    Used to store generation settings of a system object.
     */
    double MassMin;                 //Minimum mass of the object.
    double MassMax;                 //Maximum--------------------
    double MassAvgW;                //>= 0; Weight of the minimum and maximum lerp, the closer to 0, the closer the average mass is to the minimum.
    
    int[] ResMin = new int[3];      //See region RES | Resources in Main.java       Array containing the minimum initial resources.
    int[] ResMax = new int[3];      //See region RES | Resources in Main.java       ---------------------maximum-------------------
}

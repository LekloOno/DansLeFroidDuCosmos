class Card {
    /*
    Card is the representation of an HUD window.
    It has a pure display purpose, to maintain a control over the format of these windows and easily assemble them (clusters)
    */
    String[] lines;     //The display string arranged line by line in an array.
    Vector2 dimension;  //The dimensions of the contained lines. dimension.y is the number of lines, dimension.x is the length of a line.
}
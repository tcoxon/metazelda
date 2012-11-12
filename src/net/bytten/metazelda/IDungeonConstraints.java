package net.bytten.metazelda;

public interface IDungeonConstraints {

    public boolean validRoomCoords(Coords c);
    public int numberSpaces();
    public int numberKeys();
    public Coords initialCoords();
    
}

package net.bytten.metazelda;

public class Bounds {

    public final int left, top, right, bottom;
    
    public Bounds(int x, int y, int right, int bottom) {
        this.left = x;
        this.top = y;
        this.right = right;
        this.bottom = bottom;
    }
    
    public int width() {
        return right - left + 1;
    }
    
    public int height() {
        return bottom - top + 1;
    }
    
    public String toString() {
        return "Bounds("+left+","+top+","+right+","+bottom+")";
    }
}

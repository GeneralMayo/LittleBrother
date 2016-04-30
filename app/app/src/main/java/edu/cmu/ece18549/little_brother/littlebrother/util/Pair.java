package edu.cmu.ece18549.little_brother.littlebrother.util;

/**
 * Created by alexmaeda on 4/27/16.
 */
public class Pair<L, R> {
    private L left;
    private R right;

    public Pair(L left, R right){
        this.left = left;
        this.right = right;
    }

    public L getLeft(){
        return left;
    }

    public R getRight(){
        return right;
    }

}

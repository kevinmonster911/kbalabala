package com.kbalabala.tools.bits;

import java.util.BitSet;

/**
 * <p>
 *   封装对bit按位操作的方法集合
 *   <ul>
 *       <li>按指定位置设定bit 1 or 0</li>
 *   </ul>
 * </p>
 *
 * @author kevin
 * @since 2015-05-21 13:33
 */
public class JmbBitUtils {


    public static void main(String[] args){
        BitSet bitSet = new BitSet();
        bitSet.set(4, true);
        bitSet.set(100, true);
        bitSet.set(21, true);

        System.out.println(bitSet.toString());
        System.out.println(bitSet.get(10));
    }

}

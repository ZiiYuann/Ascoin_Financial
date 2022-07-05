package com.tianli.tool.captcha;

import io.netty.util.internal.ThreadLocalRandom;

/**
 * <p>随机工具类</p>
 *
 * @author: wuhongjun
 * @version:1.0
 */
public class Randoms {
    //定义验证码字符.去除了O和I等容易混淆的字母
    public static final char ALPHA[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'G', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
            , 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static final char ALPHA2[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'G', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    public static final char ALPHA3[] = {'0', '1','2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * 产生两个数之间的随机数
     *
     * @param min 小数
     * @param max 比min大的数
     * @return int 随机数字
     */
    public static int num(int min, int max) {
        return min + ThreadLocalRandom.current().nextInt(max - min);
    }

    /**
     * 产生0--num的随机数,不包括num
     *
     * @param num 数字
     * @return int 随机数字
     */
    public static int num(int num) {
        return ThreadLocalRandom.current().nextInt(num);
    }

    public static char alpha() {
        return ALPHA[num(0, ALPHA.length)];
    }

    public static String alphaString() {
        return alphaString(2, 4);
    }

    public static String alphaString(int length2, int length3) {
        StringBuilder builder = new StringBuilder(length2 + length3);
        int index = 0;
        int sourceLen2 = ALPHA2.length;
        while (index < length2){
            builder.append(ALPHA2[num(0, sourceLen2)]);
            index++;
        }
        index = 0;
        int sourceLen3 = ALPHA3.length;
        while (index < length3){
            builder.append(ALPHA3[num(0, sourceLen3)]);
            index++;
        }
        return builder.toString();
    }
}
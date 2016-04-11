package com.kbalabala.tools;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import static com.kbalabala.tools.Constants.*;

/**
 * 积木盒子数字公共类
 *
 * <p>
 *  BigDecimal 格式化
 *  <ol>
 *   <li>以###0.##的形式格式化，如果是null返回0{@link com.kbalabala.tools.JmbNumberUtils#toDecimal2Pos(BigDecimal value)}</li>
 *   <li>以###0.00的形式格式化，如果是null返回0.00{@link com.kbalabala.tools.JmbNumberUtils#toDecimal2FixedPos(BigDecimal value)}</li>
 *   <li>以###0.00##的形式格式化，如果是null返回0.00{@link com.kbalabala.tools.JmbNumberUtils#toDecimal4Pos(BigDecimal value)}</li>
 *   <li>缩小1w倍，保留4位小数（四舍五入）{@link com.kbalabala.tools.JmbNumberUtils#narrowTenThousand(BigDecimal origin)}</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015-4-20
 */
public class JmbNumberUtils {

    public static final String FORMAT_NNNN_NN = "###0.##";
    public static final String FORMAT_NNNN_NN_FIX = "###0.00";
    public static final String FORMAT_NNNN_NNNN = "###0.00##";


    /**
     * 以###0.##的形式格式化，如果是null返回0
     *
     * @param value
     * @return
     */
    public static String toDecimal2Pos(BigDecimal value) {
        return formatBigDecimal(value, FORMAT_NNNN_NN, NUMBER_ZERO);
    }

    /**
     * 以###0.00的形式格式化，如果是null返回0.00
     * @param value
     * @return
     */
    public static String toDecimal2FixedPos(BigDecimal value) {
        return formatBigDecimal(value, FORMAT_NNNN_NN_FIX, NUMBER_ZERO_2DECIMAL);
    }


    /**
     * 以###0.00##的形式格式化，如果是null返回0.00
     *
     * @param value
     * @return
     */
    public static String toDecimal4Pos(BigDecimal value) {
        return formatBigDecimal(value, FORMAT_NNNN_NNNN, NUMBER_ZERO_2DECIMAL);
    }

    /**
     * 缩小1w倍，保留4位小数（四舍五入）
     * @param origin
     * @return
     */
    public static BigDecimal narrowTenThousand(BigDecimal origin) {
        return multiple(origin, 10000, 4, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal multiple(BigDecimal origin, int multiplier, int roundPos,int roundMode) {
        if(multiplier >= 0){
            return origin.multiply(BigDecimal.valueOf(multiplier));
        } else {
            return origin.divide(BigDecimal.valueOf(Math.abs(multiplier)), roundMode);
        }
    }

    public static String formatBigDecimal(BigDecimal value, String format, String empty) {
        if(value == null) return empty;
        return new DecimalFormat(format).format(value);
    }
}

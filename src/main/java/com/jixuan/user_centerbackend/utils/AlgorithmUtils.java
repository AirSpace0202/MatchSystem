package com.jixuan.user_centerbackend.utils;

import java.util.List;
import java.util.Objects;

import static java.lang.Math.min;

/**
 * 算法工具类
 *
 * @author JxZhang
 */
public class AlgorithmUtils {

    /**
     * 编辑距离算法，计算最相似的两组标签
     * @param tagList1 标签列表1
     * @param tagList2 标签列表2
     * @return 最短编辑距离
     */
    public static int minDistance(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int [][]dp = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i ++ ) dp[i][0] = i;

        for (int j = 0; j < m + 1; j ++ ) dp[0][j] = j;

        for(int i = 1; i <= n; i ++ ) {
            for(int j = 1; j <= m; j ++ ) {
                dp[i][j] = min(dp[i - 1][j] + 1, dp[i][j - 1] + 1);
                if (Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    dp[i][j] = min(dp[i][j], dp[i - 1][j - 1]);
                }
                else dp[i][j] = min(dp[i][j], dp[i - 1][j - 1] + 1);
            }
        }
        return dp[n][m];
    }
}

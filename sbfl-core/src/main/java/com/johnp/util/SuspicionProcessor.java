package com.johnp.util;

import com.johnp.bean.MethodInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SuspicionProcessor {

    public static double calculateTarantula(double ef, double ep, double nf, double np) {
        double failRatio = safeDiv(ef, ef + nf);
        double passRatio = safeDiv(ep, ep + np);
        return safeDiv(failRatio, failRatio + passRatio);
    }

    public static double calculateSbi(double ef, double ep) {
        return safeDiv(ef, ef + ep);
    }

    public static double calculateJaccard(double ef, double ep, double nf) {
        return safeDiv(ef, nf + ep);
    }

    public static double calculateOchiai(double ef, double ep, double nf) {
        return ef / Math.sqrt(nf * (ef + ep));
    }

    public static List<Map.Entry<String, MethodInfo>> sortSuspicion(Map<String, MethodInfo> data) {
        List<Map.Entry<String, MethodInfo>> dataList = new ArrayList<>(data.entrySet());

        // ** Sort the data in descending order of Tarantula, SBI, Jaccard, Ochai
        dataList.sort(Comparator
                .comparingDouble((Map.Entry<String, MethodInfo> entry) -> entry.getValue().getSuspiciousnessTarantula())
                .reversed()
                .thenComparing(Comparator.comparingDouble((Map.Entry<String, MethodInfo> entry) -> entry.getValue().getSuspiciousnessSbi())
                        .reversed())
                .thenComparing(Comparator.comparingDouble((Map.Entry<String, MethodInfo> entry) -> entry.getValue().getSuspiciousnessJaccard())
                        .reversed())
                .thenComparing(Comparator.comparingDouble((Map.Entry<String, MethodInfo> entry) -> entry.getValue().getSuspiciousnessOchiai())
                        .reversed())
        );
        return dataList;
    }

    private static double safeDiv(double numerator, double denominator) {
        return denominator == 0 ? 0.0 : numerator / denominator;
    }


}
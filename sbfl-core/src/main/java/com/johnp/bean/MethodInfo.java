package com.johnp.bean;

import lombok.Data;

public class MethodInfo {
    private String name;
    private int methodPasses;
    private int methodFailures;

    private double suspiciousnessTarantula;
    private double suspiciousnessSbi;
    private double suspiciousnessJaccard;
    private double suspiciousnessOchiai;

    public double getSuspiciousnessJaccard() {
        return suspiciousnessJaccard;
    }

    public void setSuspiciousnessJaccard(double suspiciousnessJaccard) {
        this.suspiciousnessJaccard = suspiciousnessJaccard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMethodPasses() {
        return methodPasses;
    }

    public void setMethodPasses(int methodPasses) {
        this.methodPasses = methodPasses;
    }

    public int getMethodFailures() {
        return methodFailures;
    }

    public void setMethodFailures(int methodFailures) {
        this.methodFailures = methodFailures;
    }

    public double getSuspiciousnessTarantula() {
        return suspiciousnessTarantula;
    }

    public void setSuspiciousnessTarantula(double suspiciousnessTarantula) {
        this.suspiciousnessTarantula = suspiciousnessTarantula;
    }

    public double getSuspiciousnessSbi() {
        return suspiciousnessSbi;
    }

    public void setSuspiciousnessSbi(double suspiciousnessSbi) {
        this.suspiciousnessSbi = suspiciousnessSbi;
    }

    public double getSuspiciousnessOchiai() {
        return suspiciousnessOchiai;
    }

    public void setSuspiciousnessOchiai(double suspiciousnessOchiai) {
        this.suspiciousnessOchiai = suspiciousnessOchiai;
    }

    public MethodInfo(String name) {
        this.name = name;
        methodPasses = 0;
        methodFailures = 0;
    }


}
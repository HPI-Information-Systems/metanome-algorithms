package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;
import de.metanome.algorithms.tireless.regularexpression.matcherclasses.MatcherFactory;

public class NeedlemanWunschAlignment extends Alignment {

    protected double gapCostLeft = -1;
    protected double gapCostRight = -1;
    protected double matchMismatchWeight = 1;

    public NeedlemanWunschAlignment(RegularExpressionConjunction left, RegularExpressionConjunction right) {
        super(left, right);
    }

    @Override
    public RegularExpressionConjunction mergeExpressions() {
        int[][] originMatrix = alignAndGetOriginMatrix();
        return backtrackMatrixAndMerge(originMatrix);
    }

    public int[][] alignAndGetOriginMatrix() {
        double[][] weightMatrix = initializeWeightMatrix();
        int[][] originMatrix = initializeOriginMatrix();
        computeMatrices(weightMatrix, originMatrix);
        return originMatrix;
    }

    private void computeMatrices(double[][] weightMatrix, int[][] originMatrix) {
        for (int i = 1; i <= left.getLength(); i++) {
            for (int j = 1; j <= right.getLength(); j++) {
                computeValue(weightMatrix, originMatrix, i, j);
            }
        }
    }

    public RegularExpressionConjunction backtrackMatrixAndMerge(int[][] originMatrix) {
        RegularExpressionConjunction result = new RegularExpressionConjunction();
        int i = left.getLength();
        int j = right.getLength();
        while ((i > 0 || j > 0) && i >= 0 && j >= 0) {
            switch (originMatrix[i][j]) {
                case 1 -> appendAsOptional(result, left.getChild(i-- - 1));
                case 2 -> merge(result, left.getChild(i-- - 1), right.getChild(j-- - 1));
                case 3 -> appendAsOptional(result, right.getChild(j-- - 1));
            }
        }
        return result;
    }

    private void merge(RegularExpressionConjunction targetExpression, RegularExpression left, RegularExpression right) {
        MatcherFactory factory = new MatcherFactory();
        targetExpression.addChild(factory.getMatcher(left, right).mergeExpressions(), 0);
    }

    private void appendAsOptional(RegularExpressionConjunction targetExpression, RegularExpression appendExpression) {
        RegularExpression newChild = (RegularExpression) appendExpression.cloneRegex();
        newChild.setMinCount(0);
        targetExpression.addChild(newChild, 0);
    }

    private void computeValue(double[][] weightMatrix, int[][] originMatrix, int i, int j) {
        double matchMismatch = weightMatrix[i - 1][j - 1] +
                getSimilarity(left.getChild(i - 1), right.getChild(j - 1));
        double leftGap = weightMatrix[i - 1][j] + getGapCostLeft();
        double rightGap = weightMatrix[i][j - 1] + getGapCostRight();

        if (matchMismatch >= leftGap && matchMismatch >= rightGap) {
            weightMatrix[i][j] = matchMismatch;
            originMatrix[i][j] = 2;
        } else if (leftGap > rightGap) {
            weightMatrix[i][j] = leftGap;
            originMatrix[i][j] = 1;
        } else {
            weightMatrix[i][j] = rightGap;
            originMatrix[i][j] = 3;
        }
    }

    public double getGapCostLeft() {
        return gapCostLeft;
    }

    public void setGapCostLeft(double gapCostLeft) {
        this.gapCostLeft = gapCostLeft;
    }

    public double getGapCostRight() {
        return gapCostRight;
    }

    public void setGapCostRight(double gapCostRight) {
        this.gapCostRight = gapCostRight;
    }

    public double getMatchMismatchWeight() {
        return matchMismatchWeight;
    }

    @SuppressWarnings("unused")
    public void setMatchMismatchWeight(double matchMismatchWeight) {
        this.matchMismatchWeight = matchMismatchWeight;
    }

    private double[][] initializeWeightMatrix() {
        double[][] matrix = new double[left.getLength() + 1][right.getLength() + 1];
        matrix[0][0] = 0;
        for (int i = 1; i <= left.getLength(); i++)
            matrix[i][0] = matrix[i - 1][0] + getGapCostLeft();
        for (int i = 1; i <= right.getLength(); i++)
            matrix[0][i] = matrix[0][i - 1] + getGapCostRight();

        return matrix;
    }

    private int[][] initializeOriginMatrix() {
        int[][] matrix = new int[left.getLength() + 1][right.getLength() + 1];
        matrix[0][0] = 0;
        for (int i = 1; i <= left.getLength(); i++)
            matrix[i][0] = 1;
        for (int i = 1; i <= right.getLength(); i++)
            matrix[0][i] = 3;

        return matrix;
    }

    private double getSimilarity(RegularExpression left, RegularExpression right) {
        return new MatcherFactory(true).getMatcher(left, right).getSimilarity() * matchMismatchWeight;
    }
}

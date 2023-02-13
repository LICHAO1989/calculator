package com.richard.project.calculator.core;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Data
public class Calculator {

    private List<BigDecimal> inputNumList = new ArrayList<>(); // 输入值
    private List<String> operationList = new ArrayList<>(); // 操作符
    private List<BigDecimal> resultList = new ArrayList<>(); // 计算结果
    private String currentOperation; // 当前操作符
    private BigDecimal lastResult; // 前面计算结果
    private BigDecimal currentInput; // 当前输入
    private int currentDoIndex = -1; // 当前undo/redo操作索引


    /**
     * 点击确认计算（=）
     */
    public BigDecimal calculate() {
        if (currentInput != null) { // 新输入值
            // 累加计算
            BigDecimal result = calculateResult(lastResult, currentOperation, currentInput);
            if (this.currentDoIndex == -1) {
                // 未操作过redo/undo
                resultList.add(lastResult);
                inputNumList.add(currentInput);
                operationList.add(currentOperation);
            } else {
                // 有操作过redo/undo
                this.currentDoIndex++;

                //清除lastOptIndex之后的计算结果
                if (this.resultList.size() > this.currentDoIndex + 1) {
                    this.resultList = resultList.subList(0, this.currentDoIndex);
                }
                if (this.inputNumList.size() > this.currentDoIndex) {
                    this.inputNumList = inputNumList.subList(0, this.currentDoIndex - 1);
                }
                if (this.operationList.size() > this.currentDoIndex - 1) {
                    this.operationList = operationList.subList(0, this.currentDoIndex);
                }

                this.resultList.add(result);
                this.inputNumList.add(currentInput);
                this.operationList.add(currentOperation);
            }
            lastResult = result;
            currentOperation = null;
            currentInput = null;
        }
        return lastResult;
    }

    /**
     * 下一步
     */
    public void redo() {
        if (currentDoIndex <= -1)
            return;
        if (currentDoIndex + 1 == resultList.size()) {
            System.out.println("无法再redo!");
            return;
        }
        currentDoIndex++;

        System.out.println("redo后值:" + resultList.get(currentDoIndex) + ",redo前值:" + lastResult + ",redo的操作:" + operationList.get(currentDoIndex - 1) + ",redo操作的值:" + inputNumList.get(currentDoIndex - 1));
        lastResult = resultList.get(currentDoIndex);
        currentOperation = null;
        currentInput = null;

    }

    /**
     * 上一步
     */
    public void undo() {
        if (lastResult != null && currentDoIndex == -1) { // 未进行undo/redo操作,存储最后计算结果
            resultList.add(lastResult);
            currentOperation = null;
            currentInput = null;
        }

        if (resultList.size() == 0) {
            System.out.println("无操作!");
        } else if (resultList.size() == 1) {
            System.out.println("undo后值:0," + "undo前值:" + lastResult);
            lastResult = BigDecimal.ZERO;
        } else {
            if (currentDoIndex == -1) {
                currentDoIndex = operationList.size() - 1;
            } else {
                if (currentDoIndex - 1 < 0) {
                    System.out.println("无法再undo!");
                    return;
                }
                currentDoIndex--;
            }

            System.out.println("undo后值:" + resultList.get(currentDoIndex) + ",undo前值:" + lastResult + ",undo的操作:" + operationList.get(currentDoIndex) + ",undo操作的值:" + inputNumList.get(currentDoIndex));
            lastResult = resultList.get(currentDoIndex);
            currentOperation = null;
            currentInput = null;
        }
    }


    /**
     * 进行累计计算
     *
     * @param lastResult       前面已累计值
     * @param currentOperation 当前操作a
     * @param currentInput     新输入值
     * @return 计算结果
     */
    private BigDecimal calculateResult(BigDecimal lastResult, String currentOperation, BigDecimal currentInput) {
        BigDecimal result = BigDecimal.ZERO;
        currentOperation = currentOperation == null ? "+" : currentOperation;
        switch (currentOperation) {
            case "+":
                result = lastResult.add(currentInput);
                break;
            case "-":
                result = lastResult.subtract(currentInput).setScale(2, RoundingMode.HALF_UP);
                break;
            case "*":
                result = lastResult.multiply(currentInput).setScale(2, RoundingMode.HALF_UP);
                break;
            case "/":
                result = lastResult.divide(currentInput, RoundingMode.HALF_UP);
                break;
        }
        return result;
    }

    public void setCurrentInput(BigDecimal input) {
        if (lastResult == null) {
            // 第一次输入
            lastResult = input;
        } else {
            this.currentInput = input;
        }
    }


    /**
     * 显示操作结果
     */
    public String display() {
        StringBuilder sb = new StringBuilder();
        if (lastResult != null) {
            sb.append(lastResult.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
        }
        if (currentOperation != null) {
            sb.append(currentOperation);
        }
        if (currentInput != null) {
            sb.append(currentInput);
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setCurrentInput(BigDecimal.valueOf(3));//1: 3
        calculator.setCurrentOperation("+");
        calculator.setCurrentInput(BigDecimal.valueOf(5));//2: 3+5=8
        calculator.display();
        calculator.calculate();
        calculator.display();

        calculator.setCurrentOperation("*");
        calculator.setCurrentInput(BigDecimal.valueOf(2));//3: (3+5)*2=16
        calculator.display();
        calculator.calculate();

        calculator.setCurrentOperation("+");
        calculator.setCurrentInput(BigDecimal.valueOf(4));//4: (3+5)*2+4=20
        calculator.display();
        calculator.calculate();

        calculator.setCurrentOperation("-");
        calculator.setCurrentInput(BigDecimal.valueOf(3));//5: (3+5)*2+4-3=17
        calculator.display();
        calculator.calculate();

        calculator.display();
        calculator.undo();//step4: (3+5)*2+4=20
        calculator.display();

        calculator.display();
        calculator.undo();//step3: (3+5)*2=16
        calculator.display();

        calculator.display();
        calculator.undo();//step2: (3+5)=8
        calculator.display();


        System.out.println("开始打断undo并附加额外计算:+2");
        calculator.setCurrentOperation("+");
        calculator.setCurrentInput(BigDecimal.valueOf(7));
        calculator.display();
        calculator.calculate();
        calculator.display();
        System.out.println("打断计算结束,重新进行undo/redo操作!");

        calculator.undo();
        calculator.display();
        calculator.undo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();

        calculator.setCurrentOperation("+");
        calculator.setCurrentInput(BigDecimal.valueOf(3));//3: (3+5)+7+3=18
        calculator.display();
        calculator.calculate();
        calculator.display();

        calculator.setCurrentOperation("+");
        calculator.setCurrentInput(BigDecimal.valueOf(2.3));//3: (3+5)+7+3+2.3=20.3
        calculator.display();
        calculator.calculate();
        calculator.display();

        calculator.setCurrentOperation("-");
        calculator.setCurrentInput(BigDecimal.valueOf(2.4));//3: (3+5)+7+3+2.3-2.4=17.9
        calculator.display();
        calculator.calculate();
        calculator.display();
    }

}
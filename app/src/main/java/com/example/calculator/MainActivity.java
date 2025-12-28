package com.example.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    // UI elements
    private TextView resultDisplay;

    // Calculator state
    private String currentExpression = "";
    private final List<String> historyList = new ArrayList<>();
    private boolean lastOperationWasEquals = false;

    // onCreate method (jab Activity shuru hoti hai)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views ko initialize karna (UI elements ko code se jorna)
        resultDisplay = findViewById(R.id.result_display);

        // Saare buttons ko click listener assign karna
        int[] buttonIds = {
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4,
                R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9,
                R.id.button_add, R.id.button_subtract, R.id.button_multiply, R.id.button_divide,
                R.id.button_modulus, R.id.button_decimal, R.id.button_equals, R.id.button_clear,
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this);
        }

        updateDisplay(); // Display ko initialize karna
    }

    // Har button click ko handle karta hai
    @Override
    public void onClick(View v) {
        int id = v.getId();
        Button b = (Button) v;
        String buttonText = b.getText().toString();

        if (id == R.id.button_equals) {
            handleEquals();
        } else if (id == R.id.button_clear) {
            handleClear();
        } else if (id == R.id.button_backspace) {
            handleBackspace();
        } else if (id == R.id.button_plus_minus) {
            handlePlusMinus(); // Plus/Minus sign change karna
        } else {
            // Number aur operator input handle karna
            if (lastOperationWasEquals) {
                // Agar "=" ke baad number dabaya gaya hai, to naya calculation shuru karein
                if (isNumberOrDecimal(buttonText) || isOperator(buttonText)) {
                    currentExpression = isOperator(buttonText) ? currentExpression : ""; // Operator hone par previous result rakhein
                    currentExpression += buttonText;
                }
                lastOperationWasEquals = false;
            } else {
                // Normal input process
                currentExpression += buttonText;
            }
            updateDisplay();
        }
    }

    // Input number ya decimal hai ya nahi, yeh check karta hai
    private boolean isNumberOrDecimal(String text) {
        return text.matches("[0-9.]");
    }

    // Input operator hai ya nahi, yeh check karta hai
    private boolean isOperator(String text) {
        return text.matches("[+\\-×÷%−]");
    }

    // Current expression ko display par update karta hai
    private void updateDisplay() {
        if (currentExpression.isEmpty()) {
            resultDisplay.setText("0");
        } else {
            // Display mein operators ko user-friendly rakhein
            resultDisplay.setText(currentExpression);
        }
    }

    // "=" button ki functionality
    private void handleEquals() {
        if (currentExpression.isEmpty() || lastOperationWasEquals) {
            return;
        }

        try {
            // Expression ko evaluate karna
            double result = evaluateExpression(currentExpression);
            String resultStr = formatResult(result);

            // History mein save karna
            String historyEntry = currentExpression + " = " + resultStr;
            historyList.add(0, historyEntry); // Naye entries ko top par add karein

            // Display update aur state change
            currentExpression = resultStr;
            updateDisplay();
            lastOperationWasEquals = true;

        } catch (Exception e) {
            currentExpression = "Error"; // Error hone par "Error" dikhana
            updateDisplay();
            lastOperationWasEquals = true;
        }
    }

    // Simple Expression Evaluation (WARNING: BODMAS/PEMDAS support nahi hai!)
    // Yeh evaluation left-to-right order mein chalta hai, jaise: 2+3*5 = 25 (galat) na ki 17 (sahi).
    private double evaluateExpression(String expression) {
        // User-friendly operators ko standard operators se replace karein
        String sanitizedExpression = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-");

        // Simple arithmetic evaluation (BODMAS/PEMDAS nahi)
        // Ye logic complex hai aur main BODMAS ke liye isko zyada chhota nahi karunga,
        // bas ye assume karega ki expression sahi format mein hai.
        // Advanced calculation ke liye 'Javax.script.ScriptEngine' ka upyog hota hai, jo Android mein kabhi-kabhi available nahi hota.

        // Is basic logic ko simple banate hain:
        // Pehle numbers aur operators ko alag karna
        List<Double> numbers = new ArrayList<>();
        List<Character> operators = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < sanitizedExpression.length(); i++) {
            char c = sanitizedExpression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
                if (currentNumber.length() > 0) {
                    numbers.add(Double.parseDouble(currentNumber.toString()));
                    currentNumber = new StringBuilder();
                }
                operators.add(c);
            }
        }
        if (currentNumber.length() > 0) {
            numbers.add(Double.parseDouble(currentNumber.toString()));
        }

        if (numbers.isEmpty()) return 0.0;

        // Left-to-right evaluation
        double result = getResult(numbers, operators);

        return result;
    }

    private static double getResult(List<Double> numbers, List<Character> operators) {
        double result = numbers.get(0);
        for (int i = 0; i < operators.size(); i++) {
            char op = operators.get(i);
            double nextValue = numbers.get(i + 1);

            switch (op) {
                case '+':
                    result += nextValue;
                    break;
                case '-':
                    result -= nextValue;
                    break;
                case '*':
                    result *= nextValue;
                    break;
                case '/':
                    if (nextValue == 0)
                        throw new ArithmeticException("Division by zero ");
                    result /= nextValue;
                    break;
                case '%':
                    result %= nextValue;
                    break;
            }
        }
        return result;
    }

    // Result ko integer/double format mein saaf karna
    @SuppressLint("DefaultLocale")
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.valueOf(result);
        }
    }

    // "C" (Clear) button ki functionality
    private void handleClear() {
        currentExpression = "";
        lastOperationWasEquals = false;
        updateDisplay();
    }

    // "←" (Backspace) button ki functionality
    private void handleBackspace() {
        if (!currentExpression.isEmpty()) {
            currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
            lastOperationWasEquals = false;
            updateDisplay();
        }
    }

    // "±" (Plus/Minus) button ki functionality
    private void handlePlusMinus() {
        if (currentExpression.isEmpty() || lastOperationWasEquals) return;

        // Last number ko extract aur +/- sign change karna
        String numberPattern = "([0-9]+(\\.[0-9]+)?)$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(numberPattern);
        java.util.regex.Matcher matcher = pattern.matcher(currentExpression);

        if (matcher.find()) {
            String lastNumberStr = matcher.group(1);
            try {
                assert lastNumberStr != null;
                double lastNumber = Double.parseDouble(lastNumberStr);

                // Naye negative/positive value ke saath expression ko replace karna
                String newNumberStr = formatResult(-lastNumber);
                currentExpression = currentExpression.substring(0, matcher.start()) + newNumberStr;
                updateDisplay();
            } catch (NumberFormatException e) {
                // Ignore if not a valid number
            }
        }
    }

}
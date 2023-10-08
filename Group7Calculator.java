import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Group7Calculator extends Application {

    private String currentInput = "";
    private Label displayLabel;

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = createGridPane();
        VBox root = new VBox(10);
        root.getChildren().add(gridPane);
        Scene scene = new Scene(root, 250, 360);
        primaryStage.setTitle("Simple Calculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        displayLabel = new Label();
        displayLabel.setMinSize(230, 40);
        displayLabel.setStyle("-fx-background-color: #ffffff; -fx-font-size: 18;");
        displayLabel.setAlignment(Pos.CENTER_RIGHT);
        gridPane.add(displayLabel, 0, 0, 4, 1);

        // Add buttons for digits and basic arithmetic operators
        String[] buttons = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", "C", "=", "+",
                "BS" // Backspace button
        };

        int row = 1;
        int col = 0;
        for (String button : buttons) {
            Button btn = createButton(button);
            gridPane.add(btn, col, row);
            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }

        return gridPane;
    }

    private Button createButton(String text) {
        Button button = new Button();
        button.setMinSize(50, 50);
        button.setAlignment(Pos.CENTER);
        button.setStyle("-fx-background-radius: 25;"); // Set button corner radius to make it round
        if (text.equals("BS")) {
            // Use the Unicode symbol for backspace (⌫)
            Label backspaceLabel = new Label("\u232B");
            backspaceLabel.setStyle("-fx-font-size: 18;");
            button.setGraphic(backspaceLabel);
        } else {
            button.setText(text);
        }

        button.setOnAction(e -> handleButtonClick(text));
        return button;
    }


    private boolean resultDisplayed = false;

    private boolean lastInputOperator = false;

    private void handleButtonClick(String buttonText) {
        switch (buttonText) {
            case "C":
                currentInput = "";
                break;
            case "=":
                try {
                    double result = evaluateExpression(currentInput);
                    currentInput = String.valueOf(result);
                    resultDisplayed = true;
                    lastInputOperator = false; // Reset the flag
                } catch (ArithmeticException e) {
                    currentInput = "Division by zero error";
                    resultDisplayed = true;
                    lastInputOperator = false; // Reset the flag
                } catch (Exception e) {
                    currentInput = "Error";
                    resultDisplayed = true;
                    lastInputOperator = false; // Reset the flag
                }
                break;
            case "BS":
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
                break;
            default:
                if (isOperator(buttonText)) {
                    if (resultDisplayed) {
                        // Only allow operators if the result is displayed
                        currentInput = currentInput + buttonText;
                        resultDisplayed = false;
                    } else if (!currentInput.isEmpty() && lastInputOperator) {
                        // If the last input was an operator, replace it with the new one
                        currentInput = currentInput.substring(0, currentInput.length() - 1) + buttonText;
                    } else {
                        currentInput += buttonText;
                    }
                    lastInputOperator = true; // Set the flag
                } else {
                    if (resultDisplayed) {
                        // If the result is displayed, start a new expression with the digit
                        currentInput = buttonText;
                        resultDisplayed = false;
                    } else {
                        currentInput += buttonText;
                    }
                    lastInputOperator = false; // Reset the flag
                }
        }

        updateDisplay();
    }


    private boolean isOperator(String text) {
        return text.equals("+") || text.equals("-") || text.equals("*") || text.equals("/");
    }

    private void updateDisplay() {
        displayLabel.setText(currentInput);
    }

    private double evaluateExpression(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        x /= divisor; // division
                    } else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus
                double x;
                int startPos = this.pos;
                if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                return x;
            }
        }.parse();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

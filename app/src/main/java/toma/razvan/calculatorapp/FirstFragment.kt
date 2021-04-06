package toma.razvan.calculatorapp

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var inputTxt: String = "0"
    private var resultTxt: String = "0"

    private lateinit var inputTv: TextView
    private lateinit var resultTv: TextView

    private val clickListener = View.OnClickListener { view ->
        val pressedBtn: Button = view as Button
        processInput(pressedBtn.text as String)
        inputTv.text = inputTxt
        resultTv.text = resultTxt
    }

    private val longClickListener = View.OnLongClickListener { _ ->
        inputTxt = "0"
        resultTxt = "0"
        inputTv.text = inputTxt
        resultTv.text = resultTxt
        true
    }

    /**
     * This method will process all types of input
     * Then it will update the input/result strings accordingly
     */
    private fun processInput(input: String) {
        // if DEL is pressed we delete one character from the inputTxt or make inputTxt 0 if it has only one digit
        if (input == "DEL") {
            if (inputTxt != "0") {
                inputTxt = if (inputTxt.length == 1) {
                    "0"
                } else {
                    inputTxt.dropLast(1)
                }
            }
        } else {
            // clean inputTxt and prepare it for writing
            if (inputTxt == "0" && input != ".")    {
                inputTxt = ""
            }

            // try to add a point but fail if there are more consecutive points in a number
            if (input == ".") {
                if (inputTxt.contains(input)) {
                    if (inputTxt.indexOfLast { it == '.' } < inputTxt.indexOfFirst { it in "÷x-+"}) {
                        if (inputTxt[inputTxt.length - 1] in "0123456789") {
                            inputTxt += input
                        }
                    }
                } else {
                    inputTxt += input
                }
            } else {
                // try to add a sign
                if (input in "÷x-+") {
                    if (inputTxt == "") {
                        inputTxt = "0"
                    }
                    if (inputTxt[inputTxt.length - 1] in "÷x-+") {
                        // do nothing
                    } else {
                        inputTxt += input
                    }
                } else {
                    // if equal is pressed compute the result of the expression
                    if (input == "=") {
                        if (inputTxt != "") {
                            calculateOutput()
                        } else {
                            inputTxt = "0"
                        }
                    } else {
                        inputTxt += input
                    }
                }
            }
        }
    }

    /**
     * This method will get the postfix notation of the infix expression
     * and then it will pass it to another method that will calculate the result of the expression
     */
    private fun calculateOutput() {
        var postfixInput = ""
        postfixInput = if (inputTxt[inputTxt.length - 1] in "÷x-+") {
            infixToPostfix(inputTxt.dropLast(1))
        } else {
            infixToPostfix(inputTxt)
        }
        evaluatePostfix(postfixInput)
    }

    /**
     * This method will transform the given input (infix expression) to
     * postfix representation
     */
    private fun infixToPostfix(input: String): String {
        val stack: Stack<String> = Stack()
        var result = ""

        input.forEach {
            // if the character is a sign the program will work with a stack to handle the precedence of operations
            if (it in "÷x-+") {
                if (stack.isEmpty()) {
                    result += " "
                    stack.push(it.toString())
                } else {
                    if (it == '+' || it == '-') {
                        result = result.trimEnd()
                        result += " "
                        result += "${stack.pop()} "
                        stack.push(it.toString())
                    } else {
                        if (stack.peek() == "x" || stack.peek() == "÷") {
                            result = result.trimEnd()
                            result += " "
                            result += "${stack.pop()} "
                            stack.push(it.toString())
                        } else {
                            result += " "
                            stack.push(it.toString())
                        }
                    }
                }
            } else {
                // if the character is a number we add it accordingly
                // (either as another digit in a big number or as another digit in a big floating point number)
                // or we add it as a separate number
                if (it in "0123456789") {
                    if (result.isNotEmpty()) {
                        if (result[result.length - 2] in "0123456789" && result[result.length - 1] != '.') {
                            result = result.dropLast(1)
                            result += "$it "
                        } else if (result[result.length - 2] == '.') {
                            result = result.dropLast(1)
                            result += "$it "
                        } else {
                            result += "$it "
                        }
                    } else {
                        result += "$it "
                    }
                } else if (it == '.') {
                    result = result.dropLast(1)
                    result += "$it "
                }
            }
        }

        // add all the remaining operations to the postfix notation
        while (stack.isNotEmpty()) {
            result += "${stack.pop()} "
        }

        return result
    }

    /**
     * This method will evaluate the given input (postfix expression)
     * and will update the field resultTxt (the field from where the result text view gets it's value)
     */
    private fun evaluatePostfix(input: String) {
        println(input)
        val stack: Stack<String> = Stack()

        val arrayElements: List<String> = input.split(' ')

        arrayElements.forEach {
            if (it != "") {
                if (it !in "÷x-+") {
                    stack.push(it)
                } else if (it in "÷x-+"){
                    val a: Double = stack.pop().toDouble()
                    val b: Double = stack.pop().toDouble()

                    when(it) {
                        "÷" -> stack.push((b / a).toString())
                        "x" -> stack.push((a * b).toString())
                        "-" -> stack.push((b - a).toString())
                        "+" -> stack.push((a + b).toString())
                    }
                }
            }
        }

        resultTxt = stack.pop()
        resultTxt = if (resultTxt.toDouble() != resultTxt.toDouble().toInt().toDouble()) {
            String.format("%.2f", resultTxt.toDouble())
        } else {
            resultTxt.toDouble().toInt().toString()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputTv = view.findViewById(R.id.inputTV)
        inputTv.movementMethod = ScrollingMovementMethod()
        resultTv = view.findViewById(R.id.resultTV)

        val pressableBtnList: MutableList<Button> = mutableListOf(

                view.findViewById(R.id.zeroBtn),
                view.findViewById(R.id.oneBtn),
                view.findViewById(R.id.twoBtn),
                view.findViewById(R.id.threeBtn),
                view.findViewById(R.id.fourBtn),
                view.findViewById(R.id.fiveBtn),
                view.findViewById(R.id.sixBtn),
                view.findViewById(R.id.sevenBtn),
                view.findViewById(R.id.eightBtn),
                view.findViewById(R.id.nineBtn),
                view.findViewById(R.id.dotBtn),
                view.findViewById(R.id.equalBtn),
                view.findViewById(R.id.delBtn),
                view.findViewById(R.id.divisionBtn),
                view.findViewById(R.id.multiplicationBtn),
                view.findViewById(R.id.subtractionBtn),
                view.findViewById(R.id.additionBtn),

                )

        pressableBtnList.forEach {
            if (it.id == R.id.delBtn) {
                it.setOnLongClickListener(longClickListener)
            }
            it.setOnClickListener(clickListener)
        }



    }
}

package com.example.bikebuddy

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

class TermsConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termsconditions)

        // Get the text from textView3 and textView4
        val textView3: TextView = findViewById(R.id.textView3)
        val textView4: TextView = findViewById(R.id.textView4)
        val textView5: TextView = findViewById(R.id.textView5)
        val textView6: TextView = findViewById(R.id.textView6)
        val textView7: TextView = findViewById(R.id.textView7)
        val textView8: TextView = findViewById(R.id.textView8)
        val textView9: TextView = findViewById(R.id.textView9)
        val textView10: TextView = findViewById(R.id.textView10)
        val textView11: TextView = findViewById(R.id.textView11)
        val textView12: TextView = findViewById(R.id.textView12)



        // Make the "User Eligibility:" part bold in textView3
        val text1 = textView3.text.toString()
        val startIndexEligibility = text1.indexOf("User Eligibility:")
        val spannableString1 = SpannableString(text1)
        spannableString1.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexEligibility,
            startIndexEligibility + "User Eligibility:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView3.text = spannableString1

        // Make the "User Responsibilities:" part bold in textView4
        val text2 = textView4.text.toString()
        val startIndexResponsibility = text2.indexOf("User Responsibilities:")
        val spannableString2 = SpannableString(text2)
        spannableString2.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexResponsibility,
            startIndexResponsibility + "User Responsibilities:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView4.text = spannableString2

        // Make the "App Usage:" part bold in textView5
        val text3 = textView5.text.toString()
        val startIndexUsage = text3.indexOf("App Usage:")
        val spannableString3 = SpannableString(text3)
        spannableString3.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexUsage,
            startIndexUsage + "App Usage:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView5.text = spannableString3

        // Make the "User Conduct:" part bold in textView6
        val text4 = textView6.text.toString()
        val startIndexConduct = text4.indexOf("User Conduct:")
        val spannableString4 = SpannableString(text4)
        spannableString4.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexConduct,
            startIndexConduct + "User Conduct:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView6.text = spannableString4

        // Make the "Intellectual Property:" part bold in textView7
        val text5 = textView7.text.toString()
        val startIndexProperty = text5.indexOf("Intellectual Property:")
        val spannableString5 = SpannableString(text5)
        spannableString5.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexProperty,
            startIndexProperty + "Intellectual Property:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView7.text = spannableString5

        // Make the "Privacy:" part bold in textView8
        val text6 = textView8.text.toString()
        val startIndexPrivacy = text6.indexOf("Privacy:")
        val spannableString6 = SpannableString(text6)
        spannableString6.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexPrivacy,
            startIndexPrivacy + "Privacy:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView8.text = spannableString6

        // Make the "Limitation of Liability:" part bold in textView9
        val text7 = textView9.text.toString()
        val startIndexLiability = text7.indexOf("Limitation of Liability:")
        val spannableString7 = SpannableString(text7)
        spannableString7.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexLiability,
            startIndexLiability + "Limitation of Liability:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView9.text = spannableString7

        // Make the "Modification of Terms:" part bold in textView10
        val text8 = textView10.text.toString()
        val startIndexTerms = text8.indexOf("Modification of Terms:")
        val spannableString8 = SpannableString(text8)
        spannableString8.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexTerms,
            startIndexTerms + "Modification of Terms:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView10.text = spannableString8

        // Make the "Termination:" part bold in textView11
        val text9 = textView11.text.toString()
        val startIndexTermination = text9.indexOf("Termination:")
        val spannableString9 = SpannableString(text9)
        spannableString9.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexTermination,
            startIndexTermination + "Termination:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView11.text = spannableString9

        // Make the "Governing Law:" part bold in textView12
        val text10 = textView12.text.toString()
        val startIndexLaw = text10.indexOf("Governing Law:")
        val spannableString10 = SpannableString(text10)
        spannableString10.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndexLaw,
            startIndexLaw + "Governing Law:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView12.text = spannableString10



        val backButton: AppCompatButton = findViewById(R.id.backTerms)
        backButton.setOnClickListener {
            onBackPressed() // Handle the back button click to go back to the previous screen (fragment)
        }
    }

    override fun onBackPressed() {
        finish() // Finish the About activity to return to the previous fragment (Account fragment)
    }
}

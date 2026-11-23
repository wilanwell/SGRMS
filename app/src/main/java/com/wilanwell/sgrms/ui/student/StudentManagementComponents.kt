package com.wilanwell.sgrms.ui.student

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.wilanwell.sgrms.data.model.StudentModel
import java.io.File

fun exportStudentListToCsv(
    context: Context,
    students: List<StudentModel>
) {
    val fileName = "student_list.csv"
    val file = File(context.cacheDir, fileName)

    val csvContent = buildString {
        appendLine("Student Name,Student Code,Email,Phone Number,Class,Gender,Status")

        students.forEach { student ->
            val row = listOf(
                student.studentName.ifBlank { "Unknown Student" },
                student.studentCode.ifBlank { "-" },
                student.email.ifBlank { "Not specified" },
                student.phoneNumber.ifBlank { "Not specified" },
                student.className.ifBlank { "Not assigned" },
                student.gender.ifBlank { "Not specified" },
                student.status.ifBlank { "Active" }
            )

            appendLine(
                row.joinToString(",") { value ->
                    "\"${value.replace("\"", "\"\"")}\""
                }
            )
        }
    }

    file.writeText(csvContent)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Student List")
        putExtra(Intent.EXTRA_TITLE, "student_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Export Student List"
        )
    )
}

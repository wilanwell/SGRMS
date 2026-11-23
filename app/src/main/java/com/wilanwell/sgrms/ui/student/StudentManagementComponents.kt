package com.wilanwell.sgrms.ui.student

import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.R
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

    val notSpecified = context.getString(R.string.not_specified)
    val csvContent = buildString {
        appendLine(context.getString(R.string.csv_student_header))

        students.forEach { student ->
            val row = listOf(
                student.studentName.ifBlank { context.getString(R.string.unknown_student_title) },
                student.studentCode.ifBlank { "-" },
                student.email.ifBlank { notSpecified },
                student.phoneNumber.ifBlank { notSpecified },
                student.className.ifBlank { context.getString(R.string.not_assigned) },
                student.gender.ifBlank { notSpecified },
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
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.student_list))
        putExtra(Intent.EXTRA_TITLE, "student_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.export_student_list)
        )
    )
}

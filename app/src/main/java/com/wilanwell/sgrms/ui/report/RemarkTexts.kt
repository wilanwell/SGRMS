package com.wilanwell.sgrms.ui.report

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.utils.RemarkTexts

/** [RemarkTexts] read from strings.xml for the current screen. */
@Composable
fun remarkTexts(): RemarkTexts {
    val pending = stringResource(R.string.remarks_pending)
    return RemarkTexts(
        unknownSubject = stringResource(R.string.remarks_unknown_subject),
        noRemarks = stringResource(R.string.remarks_none),
        pending = { count -> String.format(pending, count) }
    )
}

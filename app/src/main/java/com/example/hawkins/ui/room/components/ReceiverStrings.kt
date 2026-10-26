package com.example.hawkins.ui.room.components

import android.content.res.Resources
import com.example.hawkins.R

/** Resource-backed text referenced by the declarative receiver layout. */
internal object ReceiverStrings {
    private val ids = mapOf(
        "@string/app_name" to R.string.app_name,
        "@string/title" to R.string.title,
        "@string/subtitle" to R.string.subtitle,
        "@string/station" to R.string.station,
        "@string/channel_label" to R.string.channel_label,
        "@string/transmit_label" to R.string.transmit_label,
        "@string/motion_label" to R.string.motion_label,
        "@string/reset_view" to R.string.reset_view,
        "@string/erase_reply" to R.string.erase_reply,
        "@string/submit_reply" to R.string.submit_reply,
        "@string/world_normal" to R.string.world_normal,
        "@string/world_alternate" to R.string.world_alternate,
        "@string/a11y_world_toggle" to R.string.a11y_world_toggle,
        "@string/a11y_play_signal" to R.string.a11y_play_signal,
        "@string/a11y_rotation_toggle" to R.string.a11y_rotation_toggle,
        "@string/a11y_reset_view" to R.string.a11y_reset_view,
        "@string/a11y_frequency" to R.string.a11y_frequency,
        "@string/a11y_key_n" to R.string.a11y_key_n,
        "@string/letter_n" to R.string.letter_n,
        "@string/a11y_key_a" to R.string.a11y_key_a,
        "@string/letter_a" to R.string.letter_a,
        "@string/a11y_key_r" to R.string.a11y_key_r,
        "@string/letter_r" to R.string.letter_r,
        "@string/a11y_key_s" to R.string.a11y_key_s,
        "@string/letter_s" to R.string.letter_s,
        "@string/a11y_key_u" to R.string.a11y_key_u,
        "@string/letter_u" to R.string.letter_u,
        "@string/a11y_key_o" to R.string.a11y_key_o,
        "@string/letter_o" to R.string.letter_o,
        "@string/a11y_erase_reply" to R.string.a11y_erase_reply,
        "@string/a11y_submit_reply" to R.string.a11y_submit_reply,
        "@string/loading_status" to R.string.loading_status,
        "@string/loading_signalcaption" to R.string.loading_signalcaption,
        "@string/phase_search_0" to R.string.phase_search_0,
        "@string/phase_search_1" to R.string.phase_search_1,
        "@string/phase_locked_0" to R.string.phase_locked_0,
        "@string/phase_locked_1" to R.string.phase_locked_1,
        "@string/phase_listening_0" to R.string.phase_listening_0,
        "@string/phase_listening_1" to R.string.phase_listening_1,
        "@string/phase_reply_0" to R.string.phase_reply_0,
        "@string/phase_reply_1" to R.string.phase_reply_1,
        "@string/phase_wrong_0" to R.string.phase_wrong_0,
        "@string/phase_wrong_1" to R.string.phase_wrong_1,
        "@string/phase_solved_0" to R.string.phase_solved_0,
        "@string/phase_solved_1" to R.string.phase_solved_1,
        "@string/action_listen" to R.string.action_listen,
        "@string/action_replay" to R.string.action_replay,
        "@string/action_listening" to R.string.action_listening,
        "@string/action_solved" to R.string.action_solved,
        "@string/action_send" to R.string.action_send,
        "@string/action_clear" to R.string.action_clear,
        "@string/frequency_format" to R.string.frequency_format,
        "@string/strength_format" to R.string.strength_format,
        "@string/frequency_readout" to R.string.frequency_readout,
        "@string/error_status" to R.string.error_status,
        "@string/error_caption" to R.string.error_caption,
    )

    fun text(resources: Resources, reference: String, vararg args: Any): String {
        val id = requireNotNull(ids[reference]) { "Unknown receiver string resource: $reference" }
        return resources.getString(id, *args)
    }
}

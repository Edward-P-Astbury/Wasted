package android.system.time.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment

import android.system.time.Preferences
import android.system.time.databinding.FragmentRecastBinding

class RecastFragment : Fragment() {
    private lateinit var binding: FragmentRecastBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences
    private lateinit var prefsdb: Preferences

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        prefs.copyTo(prefsdb, key)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRecastBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        prefs.registerListener(prefsListener)
    }

    override fun onStop() {
        super.onStop()
        prefs.unregisterListener(prefsListener)
    }

    private fun init() {
        ctx = requireContext()
        prefs = Preferences(ctx)
        prefsdb = Preferences(ctx, encrypted = false)
        binding.apply {
            enabled.isChecked = prefs.isRecastEnabled
            action.editText?.setText(prefs.recastAction)
            receiver.editText?.setText(prefs.recastReceiver)
            extraKey.editText?.setText(prefs.recastExtraKey)
            extraValue.editText?.setText(prefs.recastExtraValue)
        }
    }

    private fun setup() = binding.apply {
        enabled.setOnCheckedChangeListener { _, isChecked ->
            prefs.isRecastEnabled = isChecked
        }
        action.editText?.doAfterTextChanged {
            prefs.recastAction = it?.toString()?.trim() ?: return@doAfterTextChanged
        }
        receiver.editText?.doAfterTextChanged {
            prefs.recastReceiver = it?.toString()?.trim() ?: return@doAfterTextChanged
        }
        extraKey.editText?.doAfterTextChanged {
            prefs.recastExtraKey = it?.toString()?.trim() ?: return@doAfterTextChanged
        }
        extraValue.editText?.doAfterTextChanged {
            prefs.recastExtraValue = it?.toString()?.trim() ?: return@doAfterTextChanged
        }
    }
}
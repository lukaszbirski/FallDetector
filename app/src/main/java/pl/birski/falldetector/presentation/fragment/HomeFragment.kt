package pl.birski.falldetector.presentation.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.FragmentHomeBinding
import pl.birski.falldetector.other.PermissionUtil

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var requestSinglePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        it.entries.forEachIndexed() { index, _ ->
            PermissionUtil.returnPermissionsArray()[index]
        }
        checkPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        askForPermissions()

        return binding.root
    }

    private fun checkPermissions() {
        val shouldDisplay = PermissionUtil.returnPermissionsArray()
            .map { shouldShowRequestPermissionRationale(it) }
            .none { !it }

        if (!PermissionUtil.hasMessagesPermission(requireContext()) && shouldDisplay) {
            setDialog()
        }
    }

    private fun setDialog() {
        requireContext().let {
            AlertDialog.Builder(it).apply {
                setTitle(R.string.permission_dialog_title_text)
                setCancelable(false)
                setMessage(R.string.permission_dialog_message_text)
                setPositiveButton(
                    R.string.permission_dialog_dismiss_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        askForPermissions()
                    }
                )
                create()
                show()
            }
        }
    }

    private fun askForPermissions() {
        requestSinglePermission.launch(
            PermissionUtil.returnPermissionsArray()
        )
    }
}

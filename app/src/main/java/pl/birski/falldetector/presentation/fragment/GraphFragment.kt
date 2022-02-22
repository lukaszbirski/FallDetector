package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel

@AndroidEntryPoint
class GraphFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                createView()
            }
        }
    }
}

@Composable
fun createView(
    viewModel: GraphViewModel = viewModel()
) {

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Button( onClick = {
            viewModel.startService()
        }){
            Text(
                text = "Start",
                style = TextStyle(fontSize = 15.sp)
            )
        }
        Button( onClick = {
            viewModel.stopService()
        }) {
            Text(
                text = "Stop",
                style = TextStyle(fontSize = 15.sp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    createView()
}

package com.xu.dragdemo.activities

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import com.xu.dragdemo.R
import com.xu.dragdemo.base.App
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

class MainActivity : AppCompatActivity(), View.OnDragListener {

    //  存放View
    var viewList: MutableList<View> = mutableListOf()

    companion object {
        val TAG_ACTIVITY: String = "MainActivity -> "
        val TAG_DRAG: String = "onDrag -> "
        val TAG_CORRECT: String = "Correct -> "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  添加View
        for(i in 1..12) {
            val view = Button(App.getAppContext())
            view.text = "$i"
            view.textSize = 22.0f
            view.gravity = Gravity.CENTER
            //  存储信息
            view.tag = ButtonInfo(view.text.toString(), i-1)
            view.setOnClickListener {
                Toast.makeText(this, "index ${(view.tag as ButtonInfo).index}", Toast.LENGTH_SHORT).show()
            }
            view.setOnLongClickListener {
                //  开始拖拽
                startDrag(view)
                true
            }

            viewList.add(view)
        }

        gridlayout.setOnDragListener(this)
        gridlayoutAddView(gridlayout, viewList)
    }

    /**
     * 往gridlayout上动态添加控件
     */
    fun gridlayoutAddView(gdlayout: GridLayout, views: MutableList<View>) {
        for(i in views.indices) {
            val rowSpec: GridLayout.Spec = GridLayout.spec(i / 3, 1f)
            val columnSpec: GridLayout.Spec = GridLayout.spec(i % 3, 1f)
            val params: GridLayout.LayoutParams = GridLayout.LayoutParams(rowSpec, columnSpec)

            gdlayout.addView(views[i], params)
        }
    }


    /**
     * 拖动Button
     */
    private fun startDrag(view: Button) {
        //  拖动时先隐藏View
        view.visibility = View.GONE

        //  获取View中的数据
        val tag = view.tag as ButtonInfo

        val intent = Intent()
        //  用Intent来传递数据
        intent.putExtra("data", tag)

        val dragData: ClipData = ClipData.newIntent("value", intent)

        //  拖动时候的阴影 DragShadowBuilder会默认创建一个和拖动的View一模一样的影子
        //  如果想自定义影子的样式 可以通过继承DragShadowBuilder来实现
        val shadow: View.DragShadowBuilder = View.DragShadowBuilder(view)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(dragData, shadow, null, 0)
        } else {
            view.startDrag(dragData, shadow, null, 0)
        }

    }


    /**
     * Gridlayout响应拖动
     */
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        val action: Int = event!!.action

        when (action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                //  判断是否是需要接受的数据
                if(event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
                    Log.d(TAG_DRAG, "开始拖动")
                } else {
                    return false
                }
            }
            DragEvent.ACTION_DRAG_LOCATION -> Log.d(TAG_DRAG, "移动 x:${event.x}  y:${event.y}")
            DragEvent.ACTION_DROP          -> {
                Log.d(TAG_DRAG, "释放拖动View")
                correctLocation(event)
            }
            DragEvent.ACTION_DRAG_ENDED    -> Log.d(TAG_DRAG, "停止拖动")
            else                           -> return false
        }

        return true
    }


    /**
     * 计算并修正位置
     */
    fun correctLocation(event: DragEvent?) {
        //  取出数据
        val btnInfo = event!!.clipData.getItemAt(0).intent.getSerializableExtra("data") as ButtonInfo
        val x: Float = event!!.x
        val y: Float = event!!.y

        val width: Int = gridlayout.width / 3
        val height: Int = gridlayout.height / 4

        //  在第几行第几列
        var h: Int = 0
        var l: Int = 0

        //  计算在哪个方格
        when {
            x < width                           -> l = 1
            x > width && x < (2 * width)        -> l = 2
            x > (2 * width) && x < (3 * width)  -> l = 3
        }

        when {
            y < height                            -> h = 1
            y > height && y < (2 * height)        -> h = 2
            y > (2 * height) && y < (3 * height)  -> h = 3
            y > (3 * height) && y < (4 * height)  -> h = 4
        }

        //  在数组中的索引
        val index = (l + (h - 1) * 3) - 1
        Log.d(TAG_CORRECT, "l: $l  h: $h  index: $index")

        if(index == btnInfo.index)  {
            (viewList[btnInfo.index] as Button).visibility = View.VISIBLE
            return
        }

        //  只要交换两个View数据即可
        val temp = viewList[index] as Button
        val tempText = (temp.tag as ButtonInfo).text

        (viewList[index] as Button).text = btnInfo.text
        (viewList[index] as Button).tag = ButtonInfo(btnInfo.text, index)

        (viewList[btnInfo.index] as Button).text = tempText
        (viewList[btnInfo.index] as Button).tag = ButtonInfo(tempText, btnInfo.index)

        (viewList[btnInfo.index] as Button).visibility = View.VISIBLE
        (viewList[index] as Button).visibility = View.VISIBLE

        Log.d(TAG_CORRECT, "${btnInfo.index}, ${btnInfo.text}")
        Log.d(TAG_CORRECT, "${index}, ${tempText}")
    }


    /**
     * 存放Button的数据
     * @param text Button的文本
     * @param index 在数组中的索引
     */
    class ButtonInfo(var text: String, var index: Int): Serializable
}

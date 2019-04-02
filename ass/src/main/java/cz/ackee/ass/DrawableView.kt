package cz.ackee.ass

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import java.util.LinkedList

/**
 * An image view that can be drawn on.
 * The view stores all touch events using [Path]s. When done with drawing, resulting bitmap can
 * be retrieved using [getFinalBitmap].
 */
internal class DrawableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr), OnTouchListener {

    private val paths = LinkedList<Path>()
    private val undonePaths = LinkedList<Path>()

    private var path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.RED
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f * context.resources.displayMetrics.density
    }

    private var touchX = 0f
    private var touchY = 0f

    var listener: (() -> Unit)? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setOnTouchListener(this)
    }

    val canUndo
        get() = paths.size > 0
    val canRedo
        get() = undonePaths.size > 0

    fun undo() {
        if (canUndo) {
            undonePaths.add(paths.removeLast())
            invalidate()
            listener?.invoke()
        }
    }

    fun redo() {
        if (canRedo) {
            paths.add(undonePaths.removeLast())
            invalidate()
            listener?.invoke()
        }
    }

    fun getFinalBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        canvas.save()
        canvas.scale(bitmap.width / width.toFloat(), bitmap.height / height.toFloat())
        draw(canvas)
        canvas.restore()
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in paths) {
            canvas.drawPath(p, paint)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouch(arg0: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                undonePaths.clear()
                path.reset()
                path.moveTo(x, y)
                touchX = x
                touchY = y
                invalidate()
                listener?.invoke()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - touchX)
                val dy = Math.abs(y - touchY)
                // Don't register movement if distance is smaller than 1dp in any dimension
                val touchTolerance = 1f * context.resources.displayMetrics.density
                if (dx >= touchTolerance || dy >= touchTolerance) {
                    path.quadTo(touchX, touchY, (x + touchX) / 2, (y + touchY) / 2)
                    touchX = x
                    touchY = y
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(touchX, touchY)
                paths.add(path)
                path = Path()
                invalidate()
                listener?.invoke()
            }
        }
        return true
    }

}

package com.nanyi545.www.bounceindicatorlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;


/**
 * Created by Administrator on 2017/1/24.
 */
public class BounceIndicator extends View {


    private static final String TAG1="CCC";


    private static int currentState ;
    private static final int STATE_AT_START=1;
    private static final int STATE_STRETCHING=2;
    private static final int STATE_RELEASING_TO_START=4;
    private static final int STATE_RELEASING_TO_END=5;
    private static final int STATE_AT_END=6;
    private static final int STATE_DISOLVING=7;


    private boolean preDisolving(){
        return currentState<=STATE_AT_END;
    }

    private boolean isDisolving(){
        return currentState==STATE_DISOLVING;
    }


    private void initState(){
        currentState=STATE_AT_START;
    }

    private boolean needToDrawConnectingPath(){
        return ((currentState==STATE_STRETCHING)||(currentState==STATE_RELEASING_TO_START)||(currentState==STATE_RELEASING_TO_END));
    }

    private boolean isReleasingToEnd(){
        return currentState==STATE_RELEASING_TO_END;
    }

    private void setState(int newState){
        currentState=newState;
    }
    private String getState(){
        switch (currentState){
            case STATE_AT_START:return "STATE_AT_START";
            case STATE_STRETCHING:return "STATE_STRETCHING";
            case STATE_RELEASING_TO_START:return "STATE_RELEASING_TO_START";
            case STATE_RELEASING_TO_END:return "STATE_RELEASING_TO_END";
            case STATE_AT_END:return "STATE_AT_END";
            case STATE_DISOLVING:return "STATE_DISOLVING";
        }
        return "UNDEFINED";
    }


    private void modifyState(){
        float dy = stickY - endY;
        float dx = stickX - endX;
        MathVector2D.VectorF v_stick2end = new MathVector2D.VectorF(dx, dy);

        float dy2=startY-endY;
        float dx2=startX-endX;
        MathVector2D.VectorF v_start2end = new MathVector2D.VectorF(dx2, dy2);

        if (v_stick2end.getLength() < minimumLength){
            setState(STATE_AT_START);
        } else if (v_stick2end.getLength() < distanceThreshold ) {
            setState(STATE_STRETCHING);
        } else {
            if (v_start2end.getLength()< minimumLength ){
                if (isReleasingToEnd()){
                    setState(STATE_AT_END);
                    dissovleAtEnd();
                }
            } else {
                setState(STATE_RELEASING_TO_END);
            }
        }

        if((isDisolving())&&disolveController.isFinished()){
            Log.i(TAG1,"RE-init called");
            reInitState();
        }
    }

    private void reInitState(){
        setState(STATE_AT_START);
        currentCount=0;
        startX=stickX;
        startY=stickY;
        endX=stickX;
        endY=stickY;
    }


    private void toStart(){
        if (currentState==STATE_STRETCHING){
            float dx = stickX - mLastX;
            float dy = stickY - mLastY;
            if (toStartScroller.isFinished())
            toStartScroller.startScroll((int)mLastX, (int)mLastY, (int)dx, (int)dy,300);
        }
    }

    private void toEnd(){
        if (currentState==STATE_RELEASING_TO_END){
            float dx = endX - startX;
            float dy = endY - startY;
            if (toEndScroller.isFinished()){
                toEndScroller.startScroll((int) startX, (int) startY, (int) dx, (int) dy,300);
            }
        }
    }



    /**
     *  for disolving...
     */
    private float[] disolveXseed=new float[15];
    private float[] disolveYseed=new float[15];
    private float[] disolveX=new float[15];
    private float[] disolveY=new float[15];

    private float disolveR=0;

    private static final int MAX_DISOLVE_STAGE=255;
    private void dissovleAtEnd(){
        if(disolveController.isFinished()){
            setState(STATE_DISOLVING);
            toEndScroller.forceFinished(true);
            disolveController.startScroll(0,0,0,MAX_DISOLVE_STAGE,1000);
            invalidate();
        }
    }

    Paint disolvePaint;


    private void initDisolve(){
        for (int ii=0;ii<disolveXseed.length;ii++){
            disolveXseed[ii]= (float) Math.random()-0.5f;
            disolveYseed[ii]= (float) Math.random()-0.5f;
        }
    }

    private void drawDisolv(Canvas canvas){
        if (currentState == STATE_DISOLVING  &&  toEndScroller.isFinished() ){
            for (int ii=0;ii<disolveX.length;ii++){
                canvas.drawCircle( disolveX[ii]+endX,disolveY[ii]+endY,disolveR, disolvePaint);
            }
        }
    }


    public BounceIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BounceIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BounceIndicator(Context context) {
        super(context);
        init();
    }


    private void init(){

        textP=new TextPaint();
        int pixel= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics());
        textP.setTextSize(pixel);
        textP.setColor(Color.rgb(255,255,255));
        textP.setFlags(TextPaint.ANTI_ALIAS_FLAG);
        textP.setTextAlign(Paint.Align.CENTER);

        bgPaint=new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.RED);

        testPaint=new Paint();
        testPaint.setAntiAlias(true);
        testPaint.setColor(Color.GREEN);


        toStartScroller=new Scroller(getContext(),new OvershootInterpolator(2));
        toEndScroller=new Scroller(getContext());
        disolveController=new Scroller(getContext());


        connectingPath =new Path();
        initState();

        initConnectingPath();

        disolvePaint=new Paint();
        disolvePaint.setColor(Color.RED);
        disolvePaint.setAntiAlias(true);

        initDisolve();
    }



    private void initConnectingPath(){

        float distance= (float) Math.sqrt((startX-endX)*(startX-endX)+(startY-endY)*(startY-endY)) / (distanceThreshold);

        float ratio=initialRatio/(distance+1f);

        startR=endR*ratio;

        connectingPath =new Path();

        PointF startPoint_1 =new PointF(startX,startY);
        PointF startPoint_2 =new PointF(startX,startY);
        PointF endPoint_1=new PointF(endX,endY);
        PointF endPoint_2=new PointF(endX,endY);

        MathVector2D.VectorF v1=new MathVector2D.VectorF( endX-startX, endY-startY );

        v1.scaleTo(startR);
        v1.addAngle(-90);
        startPoint_1.offset(v1.dx,v1.dy);
        v1.addAngle(180);
        startPoint_2.offset(v1.dx,v1.dy);


        MathVector2D.VectorF v2=new MathVector2D.VectorF( endX-startX, endY-startY );

        v2.scaleTo(endR*0.9f);
        v2.addAngle(-90);
        endPoint_1.offset(v2.dx,v2.dy);

        v2.addAngle(180);
        endPoint_2.offset(v2.dx,v2.dy);

        PointF ctrlP=new PointF( endX/2+startX/2 , endY/2 + startY/2  );

        connectingPath.moveTo(startPoint_1.x, startPoint_1.y);
        connectingPath.quadTo(ctrlP.x,ctrlP.y,endPoint_1.x,endPoint_1.y);
        connectingPath.lineTo(endPoint_2.x,endPoint_2.y);
        connectingPath.quadTo(ctrlP.x,ctrlP.y, startPoint_2.x, startPoint_2.y);
        connectingPath.lineTo(startPoint_1.x, startPoint_1.y);

    }



    private int vWidth,vHeight;
    Rect bgRect;
    private boolean drawBg=true;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        measureHintText();

        int widthSize=measureDimension(getMinWidth(),widthMeasureSpec);
        int heightSize=measureDimension(getMinHeight(), heightMeasureSpec);
        vWidth=widthSize;
        vHeight=heightSize;
        bgRect=new Rect(0,0,vWidth,vHeight);

        mLastX=vWidth/2;
        mLastY=vHeight/2;

        stickX=vWidth/2;
        stickY=vHeight/2;

        startX=stickX;
        startY=stickY;
        startR=vHeight/2;

        endX=stickX;
        endY=stickY;
        endR=vHeight/2;

        distanceThreshold=endR*8;
        minimumLength=endR/4;
        disolveR=endR/5;

        setMeasuredDimension(widthSize, heightSize);
    }

    private int getMinHeight() {
        if (textRect!=null) return textRect.height()*2;
        else return 50;
    }
    private int getMinWidth() {
        return getMinHeight();
    }


    private int measureDimension(int defaultSize, int measureSpec) {
        int result=0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode){
            case MeasureSpec.UNSPECIFIED:
                result = defaultSize;
                break;
            case MeasureSpec.AT_MOST:   //  -----> wrap_content   !!!!!
                result = Math.min(defaultSize, specSize);
                break;
            case MeasureSpec.EXACTLY:   // ---->  1  specifying size    2  match_parent  !!!!!!
                result=specSize;   // spec Size is   in unit px  !!!
                break;
        }
        return result;
    }



    private void measureHintText(){
        textRect=new Rect();
        textP.getTextBounds(""+currentCount,0,(""+currentCount).length(),textRect);
    }


    int currentCount=127;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawBg){
            if (bgRect != null) {
                canvas.save();
                canvas.clipRect(bgRect);
                canvas.drawColor(Color.BLUE);
                canvas.restore();
            }
        }

        if (needToDrawConnectingPath()){
            canvas.drawCircle(startX,startY,startR,bgPaint);
            canvas.drawPath(connectingPath, bgPaint);
        }

        drawTextAtXY(canvas,endX,endY);
        drawDisolv(canvas);
    }


    private void drawTextAtXY(Canvas canvas, float x, float y){
        if (preDisolving()&&currentCount>0){
            if ((currentCount+"").length()==1){
                canvas.drawCircle(x,y,vHeight/2,bgPaint);
            } else if ((currentCount+"").length()>1){
                float textWidth=textRect.width()/(currentCount+"").length()*((currentCount+"").length()-1.7f);
                canvas.drawCircle(x-textWidth,y,vHeight/2,bgPaint);
                canvas.drawCircle(x+textWidth,y,vHeight/2,bgPaint);
                canvas.drawRect(new RectF(x-textWidth,y-vWidth/2,x+textWidth,y+vHeight/2),bgPaint);
            }
            canvas.drawText(""+currentCount,x,y+textRect.height()/2,textP);
        }
    }


    TextPaint textP;
    Rect textRect;
    Paint bgPaint,testPaint;


    float mLastX,mLastY;  // last position of touch
    float stickX,stickY;  //  center of the view , should not change
    float startX,startY,startR,endX,endY,endR;   // start circle  --> around stick point,   end circle --> around touch point
    float distanceThreshold;
    float initialRatio=1f;  //  ratio= startR / endR

    float minimumLength;

    Path connectingPath;   // path connecting the start and end circle


    private Scroller toEndScroller,toStartScroller,disolveController;


    private boolean ignoreTouch(){
        return ((currentCount<=0)||isDisolving());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (ignoreTouch()) return true;

        int action = event.getActionMasked();
        float xTouch = event.getX();
        float yTouch = event.getY();
        mLastX = xTouch;
        mLastY = yTouch;
        endX=mLastX;
        endY=mLastY;


        modifyState();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = xTouch-mLastX;
                float deltaY = yTouch-mLastY;

                if (needToDrawConnectingPath()){
                    initConnectingPath();
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                toStart();
                toEnd();
                invalidate();
                break;

        }


        return true;
    }






    @Override
    public void computeScroll() {
        modifyState();
        if (toStartScroller.computeScrollOffset()) {
            mLastX=toStartScroller.getCurrX();
            mLastY=toStartScroller.getCurrY();
            endX=mLastX;
            endY=mLastY;
            if (needToDrawConnectingPath()) initConnectingPath();
            invalidate();
        }
        if (toEndScroller.computeScrollOffset()) {
            startX=toEndScroller.getCurrX();
            startY=toEndScroller.getCurrY();
            if (needToDrawConnectingPath()) initConnectingPath();
            invalidate();
        }

        if (disolveController.computeScrollOffset()){

            int alpha=MAX_DISOLVE_STAGE-disolveController.getCurrY();
            int progress=disolveController.getCurrY();

            disolvePaint.setAlpha(alpha);

            for (int ii=0;ii<disolveX.length;ii++){
                disolveX[ii]= disolveXseed[ii]*progress/MAX_DISOLVE_STAGE*2*(2*endR);
                disolveY[ii]= disolveYseed[ii]*progress/MAX_DISOLVE_STAGE*2*(2*endR);
            }

            invalidate();
        }

    }



    public void addCount(){
        currentCount+=1;
        invalidate();
    }



}

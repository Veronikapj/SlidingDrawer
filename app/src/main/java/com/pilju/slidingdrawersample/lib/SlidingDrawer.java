package com.pilju.slidingdrawersample.lib;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pilju.slidingdrawersample.R;

public class SlidingDrawer extends ViewGroup {

	private final ViewDragHelper mDragHelper;

	private View mHeaderView;
	private View mDescView;

	private float mInitialMotionX;
	private float mInitialMotionY;

	private int mDragRange;
	private int mTop;
	private float mDragOffset;

	private boolean isFirstDrawing = true;
	private boolean isExpended;
	private boolean isFullOpened;
	private OnDrawerStateListener mOnDrawerStateListener;

	private int mHeight;
	private float mMaxOpenOffset = 0f;

	public interface OnDrawerStateListener {
		/**
		 * Invoked when the drawer becomes fully closed.
		 */
		void onDrawerClosed();

		/**
		 * Invoked when the drawer becomes fully open.
		 */
		void onDrawerOpened();

	}

	public SlidingDrawer(Context context) {
		this(context, null);
	}

	public SlidingDrawer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	protected void onFinishInflate() {
		mHeaderView = findViewById(R.id.viewHeader);
		mDescView = findViewById(R.id.viewDesc);
	}

	public SlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
		mDragOffset = 1f; // 초기 close;

	}

	public OnDrawerStateListener getOnDrawerStateListener() {
		return mOnDrawerStateListener;
	}

	public void setOnDrawerStateListener(
			OnDrawerStateListener onDrawerStateListener) {
		this.mOnDrawerStateListener = onDrawerStateListener;
	}

	public void openDrawer() {
		smoothSlideTo(mMaxOpenOffset);
	}
	
	public void halfOpenDrawer() {
		smoothSlideTo(0.5f);
	}

	public void closeDrawer() {
		smoothSlideTo(1f);
	}
	
	/**
	 * 키보드가 올라와서 화면이 원래 화면의 높이보다 작아지는 경우, 그 작아진 높이까지 레이어를 내려줌.
	 * @param height
	 */
	public void closeDrawerWhenKeyboardOpened(int height) {
		smoothSlideTo(height);
	}

	public boolean isExpanded() {
		return isExpended;
	}
	
	public boolean isFullOpened() {
		return isFullOpened;
	}

	/**
	 * 현재 sliding drawer 높이 반환
	 * @return
     */
	public int getCurrentTop() {
		return mTop + mHeaderView.getMeasuredHeight();
	}

	/**
	 * 최대로 열리는 높이 비율
	 * @param maxOpen
     */
	public void setMaxOpenRange(float maxOpen) {
		if(maxOpen >= 0.5f)
			maxOpen = 0.45f;
		this.mMaxOpenOffset = maxOpen;
	}

	boolean smoothSlideTo(float slideOffset) {
		final int topBound = getPaddingTop();
		int y = (int) (topBound + slideOffset * mDragRange);
		if (mDragHelper
				.smoothSlideViewTo(mHeaderView, mHeaderView.getLeft(), y)) {
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}
	
	boolean smoothSlideTo(int y) {
		if (mDragHelper
				.smoothSlideViewTo(mHeaderView, mHeaderView.getLeft(), y)) {
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == mHeaderView;
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
										  int dx, int dy) {
			mTop = top;

			isFirstDrawing = false;
			mDragOffset = (float) top / mDragRange;

			requestLayout();
			invalidate();
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// int top = getPaddingTop();
			// if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)) {
			// top += mDragRange;
			// }
			// mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);

			mDescView.layout(0, mTop + mHeaderView.getMeasuredHeight(),
					mDescView.getMeasuredWidth(), MeasureSpec.getSize(getBottom()));

			mHeaderView.layout(0, mTop, mHeaderView.getMeasuredWidth(), mTop
					+ mHeaderView.getMeasuredHeight());
			invalidate();

		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return mDragRange;
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			final int topBound = getPaddingTop();
			final int bottomBound = getHeight() - mHeaderView.getHeight()
					- mHeaderView.getPaddingBottom();

			final int newTop = Math.min(Math.max(top, topBound), bottomBound);
			return newTop;
		}
		
		@Override
		public void onViewDragStateChanged(int state) {
			super.onViewDragStateChanged(state);
			
			if(state == 0) { // 0 == 멈춘상태 . 1 == 드래그.
				if (mDragOffset >= 0.96f) {
					if (isExpended) {
						if(mOnDrawerStateListener != null)
							mOnDrawerStateListener.onDrawerClosed();
						isExpended = false;
					}
				} else {
					if (!isExpended) {
						mHeight = getHeight();
						if(mOnDrawerStateListener != null)
							mOnDrawerStateListener.onDrawerOpened();
						isExpended = true;
					}
				}
				isFullOpened = (mDragOffset <= 0.07f);
			}
			
		}

	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		if ((action != MotionEvent.ACTION_DOWN)) {
			mDragHelper.cancel();
			return super.onInterceptTouchEvent(ev);
		}

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			mDragHelper.cancel();
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		boolean interceptTap = false;

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mInitialMotionX = x;
			mInitialMotionY = y;
			interceptTap = mDragHelper.isViewUnder(mHeaderView, (int) x,
					(int) y);
			
			if(getFocusedChild() != null)
				getFocusedChild().clearFocus();

			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final float adx = Math.abs(x - mInitialMotionX);
			final float ady = Math.abs(y - mInitialMotionY);
			final int slop = mDragHelper.getTouchSlop();
			if (ady > slop && adx > ady) {
				mDragHelper.cancel();
				return false;
			}
		}
		}

		return mDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mDragHelper.processTouchEvent(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();

		boolean isHeaderViewUnder = mDragHelper.isViewUnder(mHeaderView,
				(int) x, (int) y);
		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			mInitialMotionX = x;
			mInitialMotionY = y;
			break;
		}

		case MotionEvent.ACTION_UP: {
			final float dx = x - mInitialMotionX;
			final float dy = y - mInitialMotionY;
			final int slop = mDragHelper.getTouchSlop();
			if (dy * dy < slop * slop && isHeaderViewUnder) {
				if (mDragOffset == 1)
					smoothSlideTo(mMaxOpenOffset);
				else
					smoothSlideTo(1f);
			} else {
				//살짝 슬라이드 한 경우 반 높이까지만 열어준다.
				if (mDragOffset >= 0.5f) {
					if (dy < 0)
						smoothSlideTo(0.5f);
					else
						smoothSlideTo(1f);
				} else if (mDragOffset < mMaxOpenOffset + 0.2f) {
					//그 외 높이 1f까지는 열리고자 하는 높이에서 handle 정지.
					smoothSlideTo(mMaxOpenOffset);
				}
			}

			break;
		}
		}
		return isHeaderViewUnder && isViewHit(mHeaderView, (int) x, (int) y)
				|| isViewHit(mDescView, (int) x, (int) y);
	}

	private boolean isViewHit(View view, int x, int y) {
		int[] viewLocation = new int[2];
		view.getLocationOnScreen(viewLocation);
		int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		int screenX = parentLocation[0] + x;
		int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0]
				&& screenX < viewLocation[0] + view.getWidth()
				&& screenY >= viewLocation[1]
				&& screenY < viewLocation[1] + view.getHeight();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		measureChildren(
				MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
				heightMeasureSpec);
		setMeasuredDimension(
				MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mDragRange = getHeight() - mHeaderView.getHeight();
		
		if (mHeight > 0)
			mDragRange = mHeight - mHeaderView.getHeight();
		mTop = isFirstDrawing ? mDragRange : mTop;
		mDescView.layout(0, mTop + mHeaderView.getMeasuredHeight(), r,
				MeasureSpec.getSize(getBottom()));

		mHeaderView.layout(0, mTop, mHeaderView.getMeasuredWidth(), mTop
				+ mHeaderView.getMeasuredHeight());
	}
	
}

package kz.gvsx.map;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final LatLng m1 = new LatLng(52.2609858, 76.9671442);
    private static final LatLng m2 = new LatLng(52.260498, 76.9668052);
    private static final LatLng m3 = new LatLng(52.260249, 76.964083);
    private static final LatLng m4 = new LatLng(52.259386, 76.974992);
    private static final LatLng m5 = new LatLng(52.265622, 76.967253);

    private GoogleMap mMap;

    private Animator currentAnimator;
    private int shortAnimationDuration;

    private View mBottomSheet;
    private BottomSheetBehavior mBehavior;
    private TextView mMarkerTag;
    private ImageView mThumbnail;
    private TextView mMarkerSnippet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ToggleButton toggle = findViewById(R.id.satellite_button);

        // Устанавливаем обработчик событий для
        // кнопки вкл./выкл. вида со спутника.
        toggle.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mBottomSheet = findViewById(R.id.bottom_sheet);
        mMarkerTag = findViewById(R.id.marker_tag);
        mThumbnail = findViewById(R.id.thumbnail);
        mMarkerSnippet = findViewById(R.id.marker_snippet);

        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            // Добавляем обработчик событий,
            // чтобы отслеживать перемещение нижнего экрана.
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                switch (mBehavior.getState()) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        // Если нижний экран из полураскрытого состояния
                        // перемещается в самый низ экрана,
                        // то устанавливаем отступы для карты.
                        if (slideOffset <= 0)
                            setMapPaddingBottom(bottomSheet.getMinimumHeight() * slideOffset);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setPadding(0, 0, 0, mBottomSheet.getMinimumHeight());

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Добавляем маркеры на карту.
        mMap.addMarker(new MarkerOptions().position(m1).
                snippet(getString(R.string.m1_snippet))).
                setTag(getString(R.string.m1_tag));
        mMap.addMarker(new MarkerOptions().position(m2).
                snippet(getString(R.string.m2_snippet))).
                setTag(getString(R.string.m2_tag));
        mMap.addMarker(new MarkerOptions().position(m3).
                snippet(getString(R.string.m3_snippet))).
                setTag(getString(R.string.m3_tag));
        mMap.addMarker(new MarkerOptions().position(m4).
                snippet(getString(R.string.m4_snippet))).
                setTag(getString(R.string.m4_tag));
        mMap.addMarker(new MarkerOptions().position(m5).
                snippet(getString(R.string.m5_snippet))).
                setTag(getString(R.string.m5_tag));

        // Перемещаем карту и приближаем к первому маркеру.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m1, 18));

        // Устанавливаем обработчик событий для маркеров.
        mMap.setOnMarkerClickListener(marker -> {
            // Если нижний экран скрыт, то раскрываем его.
            if (mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN)
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Перемещаем камеру на выбранный маркер.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));

            String tag = (String) marker.getTag();
            mMarkerTag.setText(tag);
            mMarkerSnippet.setText(marker.getSnippet());

            // В зависимости от выбранного маркера
            // скрываем превью или показываем его,
            // добавив обработчик нажатий.
            if (tag == getString(R.string.m1_tag)) {
                mThumbnail.setVisibility(View.GONE);
            } else if (tag == getString(R.string.m2_tag)) {
                mThumbnail.setVisibility(View.VISIBLE);
                mThumbnail.setImageResource(R.drawable.m2);
                mThumbnail.setOnClickListener(view -> zoomImageFromThumb(R.drawable.m2));
            } else if (tag == getString(R.string.m3_tag)) {
                mThumbnail.setVisibility(View.GONE);
            } else if (tag == getString(R.string.m4_tag)) {
                mThumbnail.setVisibility(View.VISIBLE);
                mThumbnail.setImageResource(R.drawable.m4);
                mThumbnail.setOnClickListener(view -> zoomImageFromThumb(R.drawable.m4));
            } else if (tag == getString(R.string.m5_tag)) {
                mThumbnail.setVisibility(View.VISIBLE);
                mThumbnail.setImageResource(R.drawable.m5);
                mThumbnail.setOnClickListener(view -> zoomImageFromThumb(R.drawable.m5));
            }

            return true;
        });
    }

    private void setMapPaddingBottom(Float offset) {
        // Устанавливаем нижний отступ для карты,
        // округлив полученный offset.
        mMap.setPadding(0, 0, 0, Math.round(offset + mBottomSheet.getMinimumHeight()));
    }

    /**
     * Анимирует зум от превью к раскрытому изоражению во весь экран.
     *
     * @param imageResId id изображения
     */
    private void zoomImageFromThumb(int imageResId) {
        // Если нижний экран полностью раскрыт,
        // то устанавливаем его в положение полураскрытого.
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = findViewById(
                R.id.expanded_image);
        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        mThumbnail.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        mThumbnail.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mThumbnail.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mThumbnail.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
    }
}
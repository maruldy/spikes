package com.novoda.pianohero;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static android.view.View.GONE;

public class AndroidThingsActivity extends AppCompatActivity {

    private AndroidThingThings androidThingThings = new AndroidThingThings();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("!!!", "I'm running");
        setContentView(R.layout.activity_android_things);

        GameMvp.Model gameMvpModel = createGameMvpModel();
        GameMvp.View gameMvpView = createGameMvpView();
        GameMvp.Presenter gameMvpPresenter = new GamePresenter(gameMvpModel, gameMvpView);

        androidThingThings.open();
        gameMvpPresenter.onCreate();
    }

    private GameMvp.View createGameMvpView() {
        return new AndroidGameMvpView(
                (GameScreen) findViewById(R.id.game_screen),
                createSpeaker(),
                createScoreDisplayer()
        );
    }

    private GameMvp.Model createGameMvpModel() {
        return new GameModel(
                new SongSequenceFactory(),
                createPiano(),
                createRestartGameClickable(),
                new GameTimer(),
                new ViewModelConverter(new SimplePitchNotationFormatter()),
                new SongPlayer()
        );
    }

    private boolean isThingsDevice() {
        // TODO once targeting 'O' use constant `PackageManager.FEATURE_EMBEDDED`
        return getPackageManager().hasSystemFeature("android.hardware.type.embedded");
    }

    private Speaker createSpeaker() {
        if (isThingsDevice()) {
            PwmPiSpeaker pwmPiSpeaker = new PwmPiSpeaker();
            androidThingThings.add(pwmPiSpeaker);
            return pwmPiSpeaker;
        } else {
            return new AndroidSynthSpeaker();
        }
    }

    private Piano createPiano() {
        C4ToB5ViewPiano virtualPianoView = (C4ToB5ViewPiano) findViewById(R.id.piano_view);
        KeyStationMini32Piano keyStationMini32Piano = new KeyStationMini32Piano(this);
        androidThingThings.add(keyStationMini32Piano);

        if (isThingsDevice()) {
            virtualPianoView.setVisibility(GONE);
            return keyStationMini32Piano;
        } else {
            virtualPianoView.setVisibility(View.VISIBLE);
            return new CompositePiano(virtualPianoView, keyStationMini32Piano);
        }
    }

    private Clickable createRestartGameClickable() {
        if (isThingsDevice()) {
            GpioButtonClickable gpioButtonClickable = new GpioButtonClickable();
            androidThingThings.add(gpioButtonClickable);
            return gpioButtonClickable;
        } else {
            final View restartButton = findViewById(R.id.restart_button);
            restartButton.setVisibility(View.VISIBLE);
            return new Clickable() {
                @Override
                public void setListener(final Listener listener) {
                    restartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick();
                        }
                    });
                }
            };
        }
    }

    private ScoreDisplayer createScoreDisplayer() {
        if (isThingsDevice()) {
            RainbowHatScoreDisplayer rainbowHatScoreDisplayer = new RainbowHatScoreDisplayer();
            androidThingThings.add((rainbowHatScoreDisplayer));
            return rainbowHatScoreDisplayer;
        } else {
            final TextView scoreTextView = ((TextView) findViewById(R.id.score_text_view));
            scoreTextView.setVisibility(View.VISIBLE);
            return new ScoreDisplayer() {
                @Override
                public void display(CharSequence score) {
                    scoreTextView.setText(score);
                }

                @Override
                public void clearScore() {
                    scoreTextView.setText(null);
                }
            };
        }
    }

    @Override
    protected void onDestroy() {
        androidThingThings.close();
        super.onDestroy();
    }
}

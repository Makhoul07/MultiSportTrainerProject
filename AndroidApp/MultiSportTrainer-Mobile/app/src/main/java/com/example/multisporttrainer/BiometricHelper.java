package com.example.multisporttrainer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Reusable wrapper around {@link BiometricPrompt} so AuthActivity (cold start)
 * and MainActivity (return-to-foreground re-lock) share one prompt definition
 * and callback wiring.
 */
public final class BiometricHelper {

    /** Outcome of a biometric attempt. */
    public interface Callback {
        void onSucceeded();

        /** User canceled, pressed "Use password", or a terminal error/lockout. */
        void onFailed();
    }

    private BiometricHelper() {
        // static helper only
    }

    /** True when the device has biometric hardware AND something enrolled right now. */
    public static boolean canAuthenticate(Context context) {
        return BiometricManager.from(context)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Show the system fingerprint prompt. Callers should typically gate on
     * {@link #canAuthenticate(Context)} first to decide whether to skip it.
     */
    public static void authenticate(FragmentActivity activity, @NonNull Callback callback) {
        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                activity,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result
                    ) {
                        callback.onSucceeded();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        // "Use password" button, cancel, or lockout.
                        callback.onFailed();
                    }

                    // onAuthenticationFailed (a single non-matching scan) is left to
                    // the system prompt, which stays open for the user to retry.
                }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MultiSport Trainer")
                .setSubtitle("Confirm your fingerprint to continue")
                .setNegativeButtonText("Use password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}

package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SubscriptionStateMachine Tests")
class SubscriptionStateMachineServiceTest {

    private SubscriptionStateMachineService stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new SubscriptionStateMachineService();
    }

    // ════════════════════════════════════
    // TRANSITIONS DEPUIS ACTIVE
    // ════════════════════════════════════

    @Nested
    @DisplayName("Transitions depuis ACTIVE")
    class FromActive {

        @Test
        @DisplayName("ACTIVE → PAST_DUE : valide (paiement échoué)")
        void active_to_pastDue_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.ACTIVE,
                                    SubscriptionStatus.PAST_DUE
                            )
                    );
        }

        @Test
        @DisplayName("ACTIVE → CANCELLED : valide (résiliation)")
        void active_to_cancelled_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.ACTIVE,
                                    SubscriptionStatus.CANCELLED
                            )
                    );
        }

        @Test
        @DisplayName("ACTIVE → SUSPENDED : invalide (doit passer par PAST_DUE)")
        void active_to_suspended_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.SUSPENDED
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("ACTIVE")
                    .hasMessageContaining("SUSPENDED");
        }

        @Test
        @DisplayName("ACTIVE → ACTIVE : invalide (pas de transition vers soi-même)")
        void active_to_active_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.ACTIVE
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class);
        }
    }

    // ════════════════════════════════════
    // TRANSITIONS DEPUIS PAST_DUE
    // ════════════════════════════════════

    @Nested
    @DisplayName("Transitions depuis PAST_DUE")
    class FromPastDue {

        @Test
        @DisplayName("PAST_DUE → ACTIVE : valide (paiement réussi)")
        void pastDue_to_active_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.PAST_DUE,
                                    SubscriptionStatus.ACTIVE
                            )
                    );
        }

        @Test
        @DisplayName("PAST_DUE → SUSPENDED : valide (toutes relances épuisées)")
        void pastDue_to_suspended_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.PAST_DUE,
                                    SubscriptionStatus.SUSPENDED
                            )
                    );
        }

        @Test
        @DisplayName("PAST_DUE → CANCELLED : valide (résiliation)")
        void pastDue_to_cancelled_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.PAST_DUE,
                                    SubscriptionStatus.CANCELLED
                            )
                    );
        }

        @Test
        @DisplayName("PAST_DUE → PAST_DUE : invalide")
        void pastDue_to_pastDue_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.PAST_DUE,
                            SubscriptionStatus.PAST_DUE
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class);
        }
    }

    // ════════════════════════════════════
    // TRANSITIONS DEPUIS SUSPENDED
    // ════════════════════════════════════

    @Nested
    @DisplayName("Transitions depuis SUSPENDED")
    class FromSuspended {

        @Test
        @DisplayName("SUSPENDED → ACTIVE : valide (paiement après suspension)")
        void suspended_to_active_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.SUSPENDED,
                                    SubscriptionStatus.ACTIVE
                            )
                    );
        }

        @Test
        @DisplayName("SUSPENDED → CANCELLED : valide")
        void suspended_to_cancelled_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.SUSPENDED,
                                    SubscriptionStatus.CANCELLED
                            )
                    );
        }

        @Test
        @DisplayName("SUSPENDED → PAST_DUE : invalide")
        void suspended_to_pastDue_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.SUSPENDED,
                            SubscriptionStatus.PAST_DUE
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("SUSPENDED")
                    .hasMessageContaining("PAST_DUE");
        }
    }

    // ════════════════════════════════════
    // TRANSITIONS DEPUIS CANCELLED
    // ════════════════════════════════════

    @Nested
    @DisplayName("Transitions depuis CANCELLED")
    class FromCancelled {

        @Test
        @DisplayName("CANCELLED → ACTIVE : valide (resubscribe)")
        void cancelled_to_active_shouldBeValid() {
            assertThatNoException()
                    .isThrownBy(() ->
                            stateMachine.validateTransition(
                                    SubscriptionStatus.CANCELLED,
                                    SubscriptionStatus.ACTIVE
                            )
                    );
        }

        @Test
        @DisplayName("CANCELLED → PAST_DUE : invalide")
        void cancelled_to_pastDue_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.CANCELLED,
                            SubscriptionStatus.PAST_DUE
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class);
        }

        @Test
        @DisplayName("CANCELLED → SUSPENDED : invalide")
        void cancelled_to_suspended_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.CANCELLED,
                            SubscriptionStatus.SUSPENDED
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class);
        }

        @Test
        @DisplayName("CANCELLED → CANCELLED : invalide")
        void cancelled_to_cancelled_shouldThrow() {
            assertThatThrownBy(() ->
                    stateMachine.validateTransition(
                            SubscriptionStatus.CANCELLED,
                            SubscriptionStatus.CANCELLED
                    )
            )
                    .isInstanceOf(InvalidStateTransitionException.class);
        }
    }

    // ════════════════════════════════════
    // isAccessAllowed
    // ════════════════════════════════════

    @Nested
    @DisplayName("isAccessAllowed")
    class AccessAllowed {

        @Test
        @DisplayName("ACTIVE → accès autorisé")
        void active_shouldAllowAccess() {
            assertThat(stateMachine.isAccessAllowed(
                    SubscriptionStatus.ACTIVE
            )).isTrue();
        }

        @Test
        @DisplayName("PAST_DUE → accès autorisé (relances en cours)")
        void pastDue_shouldAllowAccess() {
            assertThat(stateMachine.isAccessAllowed(
                    SubscriptionStatus.PAST_DUE
            )).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED → accès refusé")
        void suspended_shouldDenyAccess() {
            assertThat(stateMachine.isAccessAllowed(
                    SubscriptionStatus.SUSPENDED
            )).isFalse();
        }

        @Test
        @DisplayName("CANCELLED → accès refusé")
        void cancelled_shouldDenyAccess() {
            assertThat(stateMachine.isAccessAllowed(
                    SubscriptionStatus.CANCELLED
            )).isFalse();
        }
    }

    // ════════════════════════════════════
    // canChangePlan
    // ════════════════════════════════════

    @Nested
    @DisplayName("canChangePlan")
    class CanChangePlan {

        @Test
        @DisplayName("ACTIVE → peut changer de plan")
        void active_canChangePlan() {
            assertThat(stateMachine.canChangePlan(
                    SubscriptionStatus.ACTIVE
            )).isTrue();
        }

        @Test
        @DisplayName("PAST_DUE → ne peut pas changer de plan")
        void pastDue_cannotChangePlan() {
            assertThat(stateMachine.canChangePlan(
                    SubscriptionStatus.PAST_DUE
            )).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED → ne peut pas changer de plan")
        void suspended_cannotChangePlan() {
            assertThat(stateMachine.canChangePlan(
                    SubscriptionStatus.SUSPENDED
            )).isFalse();
        }

        @Test
        @DisplayName("CANCELLED → ne peut pas changer de plan")
        void cancelled_cannotChangePlan() {
            assertThat(stateMachine.canChangePlan(
                    SubscriptionStatus.CANCELLED
            )).isFalse();
        }
    }
}
document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("resetPasswordForm");
  const emailInput = document.getElementById("resetEmail");
  const submitButton = document.getElementById("resetPasswordSubmitButton");

  if (!form || !emailInput || !submitButton) return;

  let isSubmitting = false;

  form.addEventListener("submit", (event) => {
    if (isSubmitting) {
      event.preventDefault();
      return;
    }

    const email = (emailInput.value || "").trim();
    if (email.length === 0) {
      event.preventDefault();
      return;
    }

    isSubmitting = true;
    submitButton.disabled = true;
    submitButton.textContent = "送信中...";
  });
});

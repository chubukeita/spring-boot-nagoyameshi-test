document.addEventListener("DOMContentLoaded", function() {
	const reasonInput = document.getElementById("deleteReason");
	const withdrawButton = document.getElementById("withdrawButton");
	const reasonError = document.getElementById("reasonError");
	const noticeMessage = document.getElementById("notice")
	const safetyMessage = document.getElementById("safety")
	const maxLength = 200;

	reasonInput.addEventListener("input", function() {
		const value = reasonInput.value.trim();

		if (value.length === 0) {
			withdrawButton.disabled = true;
			noticeMessage.innerHTML = "　退会理由を入力してください。（現在：" + value.length + "字）";
			noticeMessage.style.display = "inline";
			safetyMessage.style.display = "none";
		} else if (value.length > maxLength) {
			withdrawButton.disabled = true;
			noticeMessage.innerHTML = "　退会理由は200文字以内で入力してください。（現在：" + value.length + "字）";
			noticeMessage.style.display = "inline";
			safetyMessage.style.display = "none";
		} else {
			withdrawButton.disabled = false;
			safetyMessage.innerHTML = "　現在：" + value.length + "字";
			noticeMessage.style.display = "none";
			safetyMessage.style.display = "inline";
		}
	});
});

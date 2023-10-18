var fileuploadicon = $("#file-upload i");
/**
 * uploadExcel
 * Used to uplad the excel file
 */
function uploadExcel() {
	const fileUpload = (document.getElementById('repo'));
	const regex = /^([a-zA-Z0-9\s_\\.\-:])+(.xls|.xlsx)$/;
	if (regex.test(fileUpload.value.toLowerCase())) {
		let fileName = fileUpload.files[0].name;
		if (typeof (FileReader) !== 'undefined') {
			const reader = new FileReader();
			if (reader.readAsBinaryString) {
				reader.onload = (e) => {
					processExcel(reader.result);
				};
				reader.readAsBinaryString(fileUpload.files[0]);
			}
		} else {
			console.log("This browser does not support HTML5.");
			iziToast.show({
				title: 'Fail',
				message: 'This browser does not support HTML5.',
				position: 'topRight',
				timeout: 5000,
				color: 'red'
			});
		}
	} else {
		console.log("Please upload a valid Excel file.");
		iziToast.show({
			title: 'Fail',
			message: 'Please upload a valid Excel file.',
			position: 'topRight',
			timeout: 5000,
			color: 'red'
		});
	}
}

/**
 * processExcel
 * process excel file..
 */
function processExcel(data) {
	const workbook = XLSX.read(data, { type: 'binary' });
	const firstSheet = workbook.SheetNames[0];
	const excelRows = XLSX.utils.sheet_to_row_object_array(workbook.Sheets[firstSheet]);
	if (excelRows.length != 0) {
		if ($.inArray("Git_Access_Token", Object.keys(excelRows[0])) > -1 && $.inArray("Git_Repo_Url", Object.keys(excelRows[0])) > -1) {
			$('#url').val("https://github.anaplan.com/xxx/xxxxxx.git").prop('readonly', true);
			$('#password').val('it is taken from excel sheet').prop('readonly', true);
			$('#repolist').val(JSON.stringify(excelRows));
		} else {
			$('#url').val("").prop('readonly', false);
			$('#password').val('').prop('readonly', false);
			console.log("Excel Sheet should contains header name [Git_Repo_Url, Git_Access_Token].");
			iziToast.show({
				title: 'Fail',
				message: 'Excel Sheet should contains header name [Git_Repo_Url, Git_Access_Token].',
				position: 'topRight',
				timeout: 5000,
				color: 'red'
			});
		}
	} else {
		$('#url').val("").prop('readonly', false);
		$('#password').val('').prop('readonly', false);
		console.log("Excel Sheet Contains atleast one entry.");
		iziToast.show({
			title: 'Fail',
			message: 'Excel Sheet Contains atleast one entry.',
			position: 'topRight',
			timeout: 5000,
			color: 'red'
		});
	}
}

fileuploadicon.on('click', function(event) {
	event.preventDefault();
	$('#repo').val('');
	$('#url').val("").prop('disabled', false);
	$('#password').val('').prop('disabled', false);
});

import { consume, consume_post, consume_post_json } from '/js/consumewebservice.js';
const TIPO_PAGOS = 1;
const TIPO_COMPROBANTE = 0;
const BUSCAR_POR_FECHA = 0;
const BUSCAR_POR_FECHA_COMPROBANTE = 1;
const dropArea = document.getElementById('drop-area');
const fileInput = document.getElementById('fileInput');

$(function() {
	addEvents();
	setInitialDate();
	addYears();
	setActiveNavBar();
});

// Utility function to prevent default browser behavior
let preventDefaults = (e) => {
	e.preventDefault();
	e.stopPropagation();
}

let setActiveNavBar = () => {
	$('#navIndex').addClass('active');
}

let addEvents = () => {
	$('#cmdProcesar').on('click', function() {
		procesar();
	});

	$('.form-check-input').on('click', function() {
		desplegarFiltros(this);
	});

	$('#cmdBuscar').on('click', function() {
		buscar();
	});
	
	$('#cmdUpload').on('click', function(){
		upload();
	});

	// Preventing default browser behavior when dragging a file over the container
	dropArea.addEventListener('dragover', preventDefaults);
	dropArea.addEventListener('dragenter', preventDefaults);
	dropArea.addEventListener('dragleave', preventDefaults);

	// Handling dropping files into the area
	dropArea.addEventListener('drop', handleDrop);
}

let upload = async() => {
	const fileInput = document.getElementById('fileInput');
	const formData = new FormData();
	
	for(const file of fileInput.files){
		formData.append('files',file);
	}
	
	try{
		const response = await
		fetch('http://localhost:9081/uploadfiles',{
			method: 'POST',
			body: formData
		});
		
		response.json().then((respjson)=>{
			const {header:{messageStatus}, errors} = respjson;
			if(messageStatus == 0 && errors == null && response.status == 200){
				alert('Los Archivos se han subido exitosamente, no olvides presionar el botón de procesar');
				$('.fileContainer').html('');
				$('.lblMessage').text('')
			}
		});
	}catch(error){
		console.log('Error al subir archivos', error);
	}
	
}

let handleDrop = (e) => {
	e.preventDefault();

	// Getting the list of dragged files
	const files = e.dataTransfer.files;
	const dataTransfer = new DataTransfer();
	let totalFileSize = 0;
	fileInput.files = null;
	$('.fileContainer').html('');
	
	$.each(files,(i,file) => {
    	let type = file.type;
    	let size = file.size;
     	let name = file.name;
     	let iconType = type=="application/pdf" ?  "fa-solid fa-file-pdf" : "fa-regular fa-file";
     	
     	if(!(type != "application/pdf" || type != "text/xml"))
     		return;
     		
     	if(size > 3*1024*1024 || totalFileSize > 5*1024*1024)
     		return;
    	 		
    	$('.fileContainer').append(
			`<div 	class="mb-2" 
					style ="display: flex;
					    padding: .5rem;
					    border: 1px black solid;
					    border-radius: 10px;
					    align-items: center;
					    background: linear-gradient(90deg, rgba(9, 0, 157, 1) 0%, rgba(9, 9, 121, 1) 50%, rgba(0, 212, 255, 1) 100%);
					    color: white;
					    box-shadow: rgba(0, 0, 0, 0.24) 0px 3px ">
				<i class="${iconType} mr-2">
				</i>
					${name}
			</div>`)
		
		totalFileSize = totalFileSize + size; 		
     	dataTransfer.items.add(file)	
	})
	
	$('.lblMessage').text(`${files.length} Archivo(s) ${(totalFileSize/(1024*1024)).toFixed(2)} mb`)
	fileInput.files = dataTransfer.files;
}


let buscar = () => {
	let selOption = $('#searchOptions input:checked').val();
	switch (selOption) {
		case "1":
			buscarPorFecha(BUSCAR_POR_FECHA);
			break;
		case "2":
			buscarPorFecha(BUSCAR_POR_FECHA_COMPROBANTE);
			break;
		case "3":
			buscarPorProveedor();
			break;
	}
}

let buscarPorFecha = (option) => {

	let fInicial = $('#txtStartDate').val();
	let fFinal = $('#txtEndDate').val();
	if (fInicial.length > 0 && fFinal.length > 0) {
		let request = {
			fInicial: fInicial,
			fFinal: fFinal,
			findByDate: option
		}
		consume(`buscarporfecha`, request, onBuscarResponse, 0);
	} else {
		alert(`Por favor seleccione la fecha inicial y final para realizar la busqueda`);
	}
}

let onBuscarResponse = (data, processId, status) => {
	let msg = data;
	let comprobantes = msg.body.Response.comprobantes;
	let pagos = msg.body.Response.pagos;
	$('#cardCfdis').addClass('d-none');
	$('#cardComplementos').addClass('d-none');
	$('#tblcfdis tbody').html('');
	$('#tblcdp tbody').html('');

	if (comprobantes.length > 0) {
		fillCfdiTable(comprobantes, TIPO_COMPROBANTE);
		$('#cardCfdis').removeClass('d-none');
	}
	if (pagos.length > 0) {
		fillCfdiTable(pagos, TIPO_PAGOS);
		$('#cardComplementos').removeClass('d-none');
	}

	$('[data-bs-toggle="tooltip"]').tooltip();
	if (comprobantes.length > 0 || pagos.length > 0) {
		$('#resultsContainer').removeClass('d-none');
	}
}

let fillCfdiTable = (comprobantes, tipo) => {
	let formatter = new Intl.NumberFormat('en-US', {
		style: 'currency',
		currency: 'USD',
		minimumFractionDigits: 2
		// These options are needed to round to whole numbers if that's what you want.
		//minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
		//maximumFractionDigits: 0, // (causes 2500.99 to be printed as $2,501)
	});
	let trs = '';
	$.each(comprobantes, function(i, v) {
		let subFolioFiscal = (v.fcFoliofiscal).split("-");
		let tamSubFolioFiscal = ((subFolioFiscal).length - 1);
		let subNombre = (v.proveedorVO.fcnombre).substring(0, 30);
		let filePath = `${v.proveedorVO.fcRfc}/${moment(v.fdFechaComprobante, 'YYYY-MM-DD').format('YYYY')}`;
		let fileName = `${tipo == TIPO_PAGOS ? 'CDP' : v.fcUsoCFDI}_${v.proveedorVO.fcRfc}_${(v.fcFolio != null && v.fcFolio.length != 0) ? v.fcFolio : v.fcFoliofiscal}`
		trs = trs +
			`<tr data-toggle="popover" data-placement="right" data-html="true" data-content="<a href=/myfiles/${filePath}/${fileName}.pdf target=_blank>pdf</a> | <a href=/myfiles/${filePath}/${fileName}.xml target=_blank>xml</a>">
	    	<td class="text-center">
	            ${v.fdFechaComprobante}
	         </td>
	         <td class="text-center">
	             ${v.fcSerie == null ? '' : v.fcSerie}
	         </td>
	         <td class="text-center">
	             ${v.fcFolio}
	         </td>
	         <td class="text-center" data-bs-toggle="tooltip" title="${v.fcFoliofiscal}" data-bs-placement="top">
	             	...${subFolioFiscal[tamSubFolioFiscal]}
	         </td>
	         <td class="text-center" data-bs-toggle="tooltip" title="${v.proveedorVO.fcnombre}" data-bs-placement="top">
	             ${subNombre}...
	         </td>
	         <td class="text-right">
	             ${formatter.format(v.fdTotal)}
	         </td>
	         <td class="text-center" data-bs-toggle="tooltip" title="${transformUsoCfdi(v.fcUsoCFDI)}" data-bs-placement="top">
	             ${v.fcUsoCFDI}    
	         </td>
	         <td class="text-center">
	             ${v.fcFormaDePago == undefined ? 'N/A' : v.fcFormaDePago}
	         </td>
 	         <td class="text-center">
	             ${v.tipoDeComprobante}
	         </td>
         </tr>
        `
	});

	let title = tipo != TIPO_PAGOS ? $('#cfdiSize') : $('#pagoSize')
	$(title).text(`->(${comprobantes.length})`);
	let tbl = tipo != TIPO_PAGOS ? $('#tblcfdis tbody') : $('#tblcdp tbody')
	$(tbl).append(trs);
	$('[data-toggle="popover"]').popover().on('shown.bs.popover', function() {
		let this_popover = $(this);
		setTimeout(function() {
			this_popover.popover('hide');
		}, 3500);
	});
}


let buscarPorProveedor = () => {
	let year = $('#dropdownMenuLink').text();
	let rfc = $("option[value='" + $('#proveedor').val() + "']").attr('data-fcrfc')
	rfc = rfc == undefined ? '' : rfc;
	if (year.length > 0 && rfc.length > 0) {
		let request = {
			year: year,
			rfc: rfc
		}
		consume(`buscarporproveedor`, request, onBuscarResponse, 0);
	} else {
		alert('Por favor seleccione un año y un proveedor para realizar la busqueda');
	}
}

let desplegarFiltros = (selOption) => {
	let selNumber = $(selOption).val();
	if (selNumber == 1 || selNumber == 2) {
		$('#dateFilters').removeClass('d-none');
		$('#proveedorFilters').addClass('d-none');
	} else {
		$('#dateFilters').addClass('d-none');
		$('#proveedorFilters').removeClass('d-none');
	}

}

let addYears = () => {
	let actualYear = moment().year();
	for (let i = actualYear; i >= actualYear - 5; i--) {
		let item = $(`<a class="dropdown-item" href="#">${i}</a>`)
		$('#dropdownContainer .dropdown-menu').append(item);
	}
	$('#dropdownContainer').on('click', '.dropdown-item', function() {
		$('#dropdownMenuLink').text($(this).text());
	});

}

let setInitialDate = () => {
	$('.date').attr({ min: (moment().year() - 1) + '-01-01', max: (moment().add(6, 'months').format('YYYY-MM-DD')) });
}

let procesar = () => {
	$('#cmdProcesar i').addClass('fa-spin');
	$.get('/procesarcfdiprov', function(data) {
		let messageVO = data
		if (messageVO != undefined && messageVO.errors == null) {
			displayProcessed(messageVO.body.Response.processedList);
			displayUnprocessed(messageVO.body.Response.unprocessedList);
		} else {
			alert(messageVO.errors[0].message);
		}
		$('#cmdProcesar i').removeClass('fa-spin');
	});


}

let displayProcessed = (list) => {
	let trs = '';
	$.each(list, function(k, v) {
		trs = trs +
			`<tr>
						<td class="text-center">
							${v.fcSerie == null ? '' : v.fcSerie}
						</td>
						<td class="text-center">
							${v.fcFolio}
						</td>
						<td class="text-center">
							${v.fcFoliofiscal}
						</td>
						<td class="text-right">
							${v.fdTotal == undefined ? 0.00 : v.fdTotal}
						</td>
						<td class="text-center">
							${v.fcUsoCFDI}
						</td>
						<td class="text-center">
							${v.fdFechaComprobante}
						</td>
					 </tr>
					`;
	});
	let table = `<table class="table table-striped table-hover">
	                <thead>
	                    <tr>
	                        <th class="text-center">
	                            Serie
	                        </th>
	                        <th class="text-center">
	                            Folio
	                        </th>
	                        <th class="text-center">
	                            UUID
	                        </th>
	                        <th class="text-center">
	                            Total
	                        </th>
	                        <th class="text-center">
	                            Uso
	                        </th>
	                        <th class="text-center">
	                            F. Comprobante
	                        </th>
	                    </tr>
	                </thead>
	                <tbody>
	                	${trs}
	                </tbody>
	            </table>`;
	$('#processedContainer').html('');
	$('.text-success').addClass('invisible');
	$('#processedContainer').append(table);
	$('.text-success').removeClass('invisible');
}

let displayUnprocessed = (unprocessedList) => {
	let trs = '';
	$.each(unprocessedList, function(k, v) {
		trs = trs +
			`<tr>
					<td class="text-center">
						${v}
					</td>
				 </tr>
				`;
	});
	let table = `<table class="table table-striped table-hover">
	                <thead>
	                    <tr>
	                        <th class="text-center">
	                            Motivo
	                        </th>
	                    </tr>
	                </thead>
	                <tbody>
	                	${trs}
	                </tbody>
	            </table>`;
	$('#unprocessedContainer').html('');
	$('.text-warning').addClass('invisible');
	$('#unprocessedContainer').append(table);
	$('.text-warning').removeClass('invisible');
}

let transformUsoCfdi = (usoCode) => {

	switch (usoCode) {
		case "G01":
			return "Adquisición de mercancias";
		case "G02":
			return "Devoluciones, descuentos o bonificaciones";
		case "G03":
			return "Gastos en general";
		case "I01":
			return "Construcciones";
		case "I02":
			return "Mobilario y equipo de oficina por inversiones";
		case "I03":
			return "Equipo de transporte";
		case "I04":
			return "Equipo de computo y accesorios";
		case "I05":
			return "Dados, troqueles, moldes, matrices y herramental";
		case "I06":
			return "Comunicaciones telefónicas";
		case "I07":
			return "Comunicaciones satelitales";
		case "I08":
			return "Otra maquinaria y equipo";
		case "D01":
			return "Honorarios médicos, dentales y gastos hospitalarios.";
		case "D02":
			return "Gastos médicos por incapacidad o discapacidad";
		case "D03":
			return "Gastos funerales.";
		case "D04":
			return "Donativos.";
		case "D05":
			return "Intereses reales efectivamente pagados por créditos hipotecarios (casa habitación).";
		case "D06":
			return "Aportaciones voluntarias al SAR.";
		case "D07":
			return "Primas por seguros de gastos médicos.";
		case "D08":
			return "Gastos de transportación escolar obligatoria.";
		case "D09":
			return "Depósitos en cuentas para el ahorro, primas que tengan como base planes de pensiones.";
		case "D10":
			return "Pagos por servicios educativos (colegiaturas)";
		case "S01":
			return "Sin efectos fiscales";
		case "CP01":
			return "Pagos";
		case "CN01":
			return "Nómina";
	}

}

package ismaapp.tortosa.glucoseregister.utils.params

import ismaapp.tortosa.glucoseregister.services.IGlucoseService
import ismaapp.tortosa.glucoseregister.services.IPrintService

data class DialogOptionsParams(val glucoseService: IGlucoseService,
                               val printService: IPrintService,
                               val onMeasurementsDeleted: (Boolean, String) -> Unit,
                               val showDialog: Boolean,
                               val onDismiss: () -> Unit,
                               val onConfirm: () -> Unit,
                               val userSelection: String,
                               val isDatabaseEmptyOrNull: () -> Boolean)

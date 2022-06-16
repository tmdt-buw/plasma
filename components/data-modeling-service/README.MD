### Platform for Auxiliary Semantic Modeling Approaches (PLASMA)

# Data Modeling Servics (DMS)

Main component of PLASMA. This service stores ongoing semantic model creations ("modelings")
and handles model modifications.

## Environment Variables
This section covers the main environment variables that can be manipulated in order to customize PLASMA.
Set them in the docker-compose environment section to overwrite container set values.

### Optional features

If ```plasma.dms.feature.syntacticoperations.enabled``` is set to ```true```, the UI 
is provided with options to modify the syntactic model. Keep set to ```false``` (default)
if your surrounding system is unable to handle those changes.

```plasma.dms.feature.recommendations.enabled``` enables the request for recommendations
from the SRS if set to ```true```. If an instance of this service is available, 
set to true to obtain recommendations from connected auxiliary modeling services (ARS).
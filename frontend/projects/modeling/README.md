# Modeling

PLASMA (Platform for Auxiliary Semantic Modeling Approaches) modeling is an angular library that offers a visual user interface for semantic modeling and ontology management in the plasma system.
PLASMA is a platform to utilize existing and future modeling approaches in a consistent and extendable environment. PLASMA aims to support the development of new semantic refinement approaches by providing necessary supplementary functionalities.

For more information see our [github repository](https://github.com/tmdt-buw/plasma)

## Cite PLASMA as

```
@conference{paulus21,
author={Alexander Paulus. and Andreas Burgdorf. and Lars Puleikis. and Tristan Langer. and André Pomp. and Tobias Meisen.},
title={PLASMA: Platform for Auxiliary Semantic Modeling Approaches},
booktitle={Proceedings of the 23rd International Conference on Enterprise Information Systems - Volume 2: ICEIS,},
year={2021},
pages={403-412},
publisher={SciTePress},
organization={INSTICC},
doi={10.5220/0010499604030412},
isbn={978-989-758-509-8},
issn={2184-4992},
}
```
## Use the modeling library

Install the library via npm

`@tmdt-buw/pls-modeling`

Install peer dependencies required by jointjs:     
```
"@angular/cdk": ">=10.0.0",
"@angular/core": ">=10.0.0",
"@angular/flex-layout": "^10.0.0-beta.32 || ^11.0.0-beta.33 || 12.0.0-beta.34",
"ng-zorro-antd": ">=10.0.0",
"dagre": "^0.8.5",
"graphlib": "^2.1.8",
"jointjs": "^3.3.0",
"rxjs": "^6.4.0",
"svg-pan-zoom": "^3.6.1"
```

Include the modeling component in your application providing at least a modeling id from a model created in plasma:

```
<pls-modeling [modelId]="modelId"></pls-modeling>
```

Options to customize the component:

| Property            | Description                                                                                                                         | Type    | Default |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------|---------|---------|
| modelId             | Id from a model create through the plasma api.                                                                                      | string  | -       |
| fullScreen          | Whether to show the button to switch to full screen modeling.                                                                       | boolean | true    |
| viewerMode          | If viewer modes is enabled, models can only be viewed but not edited. Viewer model will disable all model adjustment functionality. | boolean | false   |
| backgroundColor     | The background-color to use in the modeling canvas in hex code (mostly relevant for theming like dark mode).                        | string  | -       |
| hideFinalizeButton  | Option to hide the finalize button to disable final model integration.                                                              | boolean | false   |
| hideExportButton    | Option to disable export via the GUI by hiding the export button.                                                                   | boolean | false   |


## Theming

The modeling library uses less variables for styling. The variables can be overwritten by creating a custom `modeling-theme.less` used by the project and overwrite the following variables:

```
@modeling-background-color: white;
@modeling-primary-color: #042042;
@modeling-text-color: white;
@modeling-highlight-color: #ff5900;

// class colors
@modeling-class-color: @modeling-primary-color;
@modeling-class-text-color: white;

// named entity colors
@modeling-named-entity-color: #019267;
@modeling-named-entity-text-color: white;

// literal colors
@modeling-literal-color: #7c3e66;
@modeling-literal-text-color: white;

// relation colors
@modeling-objectproperty-color: @modeling-primary-color;
@modeling-datatypeproperty-color: @modeling-primary-color;
```


---

## Development

### Build

Run `ng build modeling` to build the project. The build artifacts will be stored in the `dist/` directory.

### Publishing

After building your library with `ng build modeling`, go to the dist folder `cd dist/modeling` and run `npm publish`.

### Running unit tests

Run `ng test modeling` to execute the unit tests via [Karma](https://karma-runner.github.io).

## [1.1.7] - 2026-07-15

### Fixed
- **Home Layout & Interaction Reset**: Reverted generalized CSS rules (such as `button:has(svg)`) and global container overrides (`overflow: visible`, `position: relative`) that caused elements on the main arena dashboard to align incorrectly ("storte").
- **Login and Chat Selector Restoration**: Resolved the non-responsive main button bug (including login and chat type selectors). Replaced over-eager event capturing blockages with precise click-propagation filtering scoped solely to elements within chat rows, allowing main buttons to be clicked and tapped normally.
- **Robust Three-Dots Popups**: Maintained stable visibility of options buttons next to chat entries while allowing their action dropdowns to toggle normally without switching the active chat.

/**
 * global constants like  keys for keyValue pairs stored in localStorage
 * are defined in this class
 * @author Pieter Jan Bleichrodt
 */
const JWT_KEY ="jwtToken"
const CURRENT_ASSET_KEY = "currentAsset"
const ROUNDING_DIGITS = 2

function normalizePrice(currentPrice) {
    //is de prijs ver achter de komma? Dan wordt 1/currentPrice groot
    //Hoe ver achter de komma? neem er de log10 van en rond de factor af.
    let factor = Math.round(Math.log10(1 / currentPrice))
    // als de factor groter dan 1 is (dus getal is meer dan een nul achter de komma)
    //corrigeer het aantal digits achter de komma met die factor. Als factor kleiner dan 1 is rond dan af op het
    //standaard aantal cijfers achter de komma(ROUNDING_DIGITS)
    let multiPly = (factor > 1 ? Math.pow(10, factor) : 1) * Math.pow(10, ROUNDING_DIGITS)
    return (Math.round(currentPrice * multiPly) / multiPly)
}
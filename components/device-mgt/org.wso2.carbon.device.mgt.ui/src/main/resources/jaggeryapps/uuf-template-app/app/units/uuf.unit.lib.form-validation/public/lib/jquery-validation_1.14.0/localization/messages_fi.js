/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

(function( factory ) {
	if ( typeof define === "function" && define.amd ) {
		define( ["jquery", "../jquery.validate"], factory );
	} else {
		factory( jQuery );
	}
}(function( $ ) {

/*
 * Translated default messages for the jQuery validation plugin.
 * Locale: FI (Finnish; suomi, suomen kieli)
 */
$.extend($.validator.messages, {
	required: "T&auml;m&auml; kentt&auml; on pakollinen.",
	email: "Sy&ouml;t&auml; oikea s&auml;hk&ouml;postiosoite.",
	url: "Sy&ouml;t&auml; oikea URL-osoite.",
	date: "Sy&ouml;t&auml; oikea p&auml;iv&auml;m&auml;&auml;r&auml;.",
	dateISO: "Sy&ouml;t&auml; oikea p&auml;iv&auml;m&auml;&auml;r&auml; muodossa VVVV-KK-PP.",
	number: "Sy&ouml;t&auml; luku.",
	creditcard: "Sy&ouml;t&auml; voimassa oleva luottokorttinumero.",
	digits: "Sy&ouml;t&auml; pelk&auml;st&auml;&auml;n numeroita.",
	equalTo: "Sy&ouml;t&auml; sama arvo uudestaan.",
	maxlength: $.validator.format("Voit sy&ouml;tt&auml;&auml; enint&auml;&auml;n {0} merkki&auml;."),
	minlength: $.validator.format("V&auml;hint&auml;&auml;n {0} merkki&auml;."),
	rangelength: $.validator.format("Sy&ouml;t&auml; v&auml;hint&auml;&auml;n {0} ja enint&auml;&auml;n {1} merkki&auml;."),
	range: $.validator.format("Sy&ouml;t&auml; arvo v&auml;lilt&auml; {0}&ndash;{1}."),
	max: $.validator.format("Sy&ouml;t&auml; arvo, joka on enint&auml;&auml;n {0}."),
	min: $.validator.format("Sy&ouml;t&auml; arvo, joka on v&auml;hint&auml;&auml;n {0}.")
});

}));
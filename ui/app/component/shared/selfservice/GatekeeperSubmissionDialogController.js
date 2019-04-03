/*
 *
 * Copyright 2018. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import GatekeeperSubmissionDialogJustification from './model/GatekeeperSubmissionDialogJustification';

let dialog;
let explanation;
let ticketId;
let message;
let requiresExplanation;
let title;
let ticketIdFieldMessage;
let ticketIdFieldRequired;
let explanationFieldRequired;
let justification;

class GatekeeperSubmissionDialogController{
        constructor($mdDialog, $scope, message, requiresExplanation, title, ticketIdFieldMessage, ticketIdFieldRequired, explanationFieldRequired){
            dialog = $mdDialog;
            this.message = message;
            requiresExplanation = requiresExplanation;
            this.title = title;
            this.ticketIdFieldMessage = ticketIdFieldMessage;
            this.ticketIdFieldRequired = ticketIdFieldRequired;
            this.explanationFieldRequired = explanationFieldRequired;
        }

        hide() {
            justification = new GatekeeperSubmissionDialogJustification(this.explanation, this.ticketId);
            console.log('hide() ticketId: ' + justification.ticketId + ', explanation: ' + justification.explanation);
            dialog.hide(justification);
        };

        abort() {
            dialog.cancel();
        };

        getTicketIdFieldMessage() {
            let ticketIdFieldMessageToDisplay = 'Please provide a Ticket ID';
            if(this.ticketIdFieldMessage !== '') {
                ticketIdFieldMessageToDisplay = this.ticketIdFieldMessage;
            }

            if(!this.ticketIdFieldRequired) {
                ticketIdFieldMessageToDisplay += ' (Optional)';
            }

            return ticketIdFieldMessageToDisplay;
        }
    }

export default GatekeeperSubmissionDialogController;
